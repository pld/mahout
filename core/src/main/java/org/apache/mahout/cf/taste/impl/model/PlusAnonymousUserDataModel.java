/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.cf.taste.impl.model;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import com.google.common.base.Preconditions;

/**
 * <p>
 * This {@link DataModel} decorator class is useful in a situation where you wish to recommend to a user that
 * doesn't really exist yet in your actual {@link DataModel}. For example maybe you wish to recommend DVDs to
 * a user who has browsed a few titles on your DVD store site, but, the user is not yet registered.
 * </p>
 * 
 * <p>
 * This enables you to temporarily add a temporary user to an existing {@link DataModel} in a way that
 * recommenders can then produce recommendations anyway. To do so, wrap your real implementation in this
 * class:
 * </p>
 * 
 * <p>
 * 
 * <pre>
 * DataModel realModel = ...;
 * DataModel plusModel = new PlusAnonymousUserDataModel(realModel);
 * ...
 * ItemSimilarity similarity = new LogLikelihoodSimilarity(realModel); // not plusModel
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * But, you may continue to use <code>realModel</code> as input to other components. To recommend, first construct and
 * set the temporary user information on the model and then simply call the recommender. The
 * <code>synchronized</code> block exists to remind you that this is of course not thread-safe. Only one set
 * of temp data can be inserted into the model and used at one time.
 * </p>
 * 
 * <p>
 * 
 * <pre>
 * Recommender recommender = ...;
 * ...
 * synchronized(...) {
 *   PreferenceArray tempPrefs = ...;
 *   plusModel.setTempPrefs(tempPrefs);
 *   recommender.recommend(PlusAnonymousUserDataModel.TEMP_USER_ID, 10);
 *   plusModel.setTempPrefs(null);
 * }
 * </pre>
 * 
 * </p>
 */
public final class PlusAnonymousUserDataModel implements DataModel {

  public static final long TEMP_USER_ID = Long.MIN_VALUE;
  
  private final DataModel delegate;
  private PreferenceArray tempPrefs;
  private final FastIDSet prefItemIDs;
  
  public PlusAnonymousUserDataModel(DataModel delegate) {
    this.delegate = delegate;
    this.prefItemIDs = new FastIDSet();
  }
  
  public void setTempPrefs(PreferenceArray prefs) {
    Preconditions.checkArgument(prefs != null && prefs.length() > 0, "prefs is null or empty");
    this.tempPrefs = prefs;
    this.prefItemIDs.clear();
    for (int i = 0; i < prefs.length(); i++) {
      this.prefItemIDs.add(prefs.getItemID(i));
    }
  }

  public void clearTempPrefs() {
    tempPrefs = null;
    prefItemIDs.clear();
  }
  
  @Override
  public LongPrimitiveIterator getUserIDs() throws TasteException {
    if (tempPrefs == null) {
      return delegate.getUserIDs();
    }
    return new PlusAnonymousUserLongPrimitiveIterator(delegate.getUserIDs(), TEMP_USER_ID);
  }
  
  @Override
  public PreferenceArray getPreferencesFromUser(long userID) throws TasteException {
    if (userID == TEMP_USER_ID) {
      if (tempPrefs == null) {
        throw new NoSuchUserException(userID);
      }
      return tempPrefs;
    }
    return delegate.getPreferencesFromUser(userID);
  }
  
  @Override
  public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
    if (userID == TEMP_USER_ID) {
      if (tempPrefs == null) {
        throw new NoSuchUserException(userID);
      }
      return prefItemIDs;
    }
    return delegate.getItemIDsFromUser(userID);
  }
  
  @Override
  public LongPrimitiveIterator getItemIDs() throws TasteException {
    return delegate.getItemIDs();
    // Yeah ignoring items that only the plus-one user knows about... can't really happen
  }
  
  @Override
  public PreferenceArray getPreferencesForItem(long itemID) throws TasteException {
    if (tempPrefs == null) {
      return delegate.getPreferencesForItem(itemID);
    }
    PreferenceArray delegatePrefs = null;
    try {
      delegatePrefs = delegate.getPreferencesForItem(itemID);
    } catch (NoSuchItemException nsie) {
      // OK. Probably an item that only the anonymous user has
    }
    for (int i = 0; i < tempPrefs.length(); i++) {
      if (tempPrefs.getItemID(i) == itemID) {
        int length = delegatePrefs == null ? 0 : delegatePrefs.length();
        PreferenceArray newPreferenceArray = new GenericItemPreferenceArray(length + 1);
        for (int j = 0; j < length; j++) {
          newPreferenceArray.setUserID(j, delegatePrefs.getUserID(j));
          newPreferenceArray.setItemID(j, delegatePrefs.getItemID(j));
          newPreferenceArray.setValue(j, delegatePrefs.getValue(j));
        }
        newPreferenceArray.setUserID(length, tempPrefs.getUserID(i));
        newPreferenceArray.setItemID(length, tempPrefs.getItemID(i));
        newPreferenceArray.setValue(length, tempPrefs.getValue(i));
        newPreferenceArray.sortByUser();
        return newPreferenceArray;
      }
    }
    if (delegatePrefs == null) {
      // No, didn't find it among the anonymous user prefs
      throw new NoSuchItemException(itemID);
    }
    return delegatePrefs;
  }
  
  @Override
  public Float getPreferenceValue(long userID, long itemID) throws TasteException {
    if (userID == TEMP_USER_ID) {
      if (tempPrefs == null) {
        throw new NoSuchUserException(userID);
      }
      for (int i = 0; i < tempPrefs.length(); i++) {
        if (tempPrefs.getItemID(i) == itemID) {
          return tempPrefs.getValue(i);
        }
      }
      return null;
    }
    return delegate.getPreferenceValue(userID, itemID);
  }

  @Override
  public Long getPreferenceTime(long userID, long itemID) throws TasteException {
    if (userID == TEMP_USER_ID) {
      if (tempPrefs == null) {
        throw new NoSuchUserException(userID);
      }
      return null;
    }
    return delegate.getPreferenceTime(userID, itemID);
  }
  
  @Override
  public int getNumItems() throws TasteException {
    return delegate.getNumItems();
  }
  
  @Override
  public int getNumUsers() throws TasteException {
    return delegate.getNumUsers() + (tempPrefs == null ? 0 : 1);
  }
  
  @Override
  public int getNumUsersWithPreferenceFor(long... itemIDs) throws TasteException {
    if (tempPrefs == null) {
      return delegate.getNumUsersWithPreferenceFor(itemIDs);
    }
    boolean hasAll = true;
    for (long itemID : itemIDs) {
      boolean found = false;
      for (int i = 0; i < tempPrefs.length(); i++) {
        if (tempPrefs.getItemID(i) == itemID) {
          found = true;
          break;
        }
      }
      if (!found) {
        hasAll = false;
        break;
      }
    }
    return delegate.getNumUsersWithPreferenceFor(itemIDs) + (hasAll ? 1 : 0);
  }
  
  @Override
  public void setPreference(long userID, long itemID, float value) throws TasteException {
    if (userID == TEMP_USER_ID) {
      if (tempPrefs == null) {
        throw new NoSuchUserException(userID);
      }
      throw new UnsupportedOperationException();
    }
    delegate.setPreference(userID, itemID, value);
  }
  
  @Override
  public void removePreference(long userID, long itemID) throws TasteException {
    if (userID == TEMP_USER_ID) {
      if (tempPrefs == null) {
        throw new NoSuchUserException(userID);
      }
      throw new UnsupportedOperationException();
    }
    delegate.removePreference(userID, itemID);
  }
  
  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    delegate.refresh(alreadyRefreshed);
  }

  @Override
  public boolean hasPreferenceValues() {
    return delegate.hasPreferenceValues();
  }

  @Override
  public float getMaxPreference() {
    return delegate.getMaxPreference();
  }

  @Override
  public float getMinPreference() {
    return delegate.getMinPreference();
  }

}
