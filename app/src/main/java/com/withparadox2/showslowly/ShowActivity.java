package com.withparadox2.showslowly;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import com.withparadox2.showslowly.entity.Friend;
import com.withparadox2.showslowly.entity.Persist;
import com.withparadox2.showslowly.token.TokenManager;
import com.withparadox2.showslowly.util.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class ShowActivity extends AppCompatActivity {
  private List<Friend> mFriendList = new ArrayList<>();

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    //final RecyclerView textView = findViewById(R.id.rv_friend);
    //
    //Collections.sort(mFriendList, new Comparator<Friend>() {
    //  @Override public int compare(Friend o1, Friend o2) {
    //    if (o1.getLastComment() == null && o2.getLastComment() == null) {
    //      return 0;
    //    } else if (o1.getLastComment() == null) {
    //      return 1;
    //    } else if (o2.getLastComment() == null) {
    //      return -1;
    //    } else {
    //      return -o1.getLastComment().compareToIgnoreCase(o2.getLastComment());
    //    }
    //  }
    //});

    //textView.setText(sb.toString());
  }

  @Override protected void onResume() {
    super.onResume();
    TokenManager.checkTokenFromClicpBoard();
  }

  public Persist parseJson(String str) throws JSONException {
    Persist persist = new Persist();

    JSONObject jsonObject = new JSONObject(str);

    String meStr = jsonObject.optString("me");
    JSONObject meJsonObject = new JSONObject(meStr);
    persist.token = meJsonObject.optString("token");

    String contactsStr = jsonObject.optString("contacts");
    JSONObject contactsJsonObject = new JSONObject(contactsStr);
    Iterator<String> keys = contactsJsonObject.keys();
    if (keys != null) {
      while (keys.hasNext()) {
        String key = keys.next();
        if (!key.matches("\\d+")) {
          continue;
        }
        JSONObject friendJsonObject = contactsJsonObject.getJSONObject(key);
        Friend friend = new Friend();
        friend.setId(key);
        friend.setName(friendJsonObject.optString("name"));
        friend.setLastComment(friendJsonObject.optString("latest_comment"));
        friend.setLastLogin(friendJsonObject.optString("last_login"));
        String locationStr = friendJsonObject.optString("user_location");
        if (locationStr != null && locationStr.contains(",")) {
          String[] locations = locationStr.split(",");
          if (locations.length == 2) {
            friend.setLatitude(Util.parseNumber(locations[0], -1));
            friend.setLongitude(Util.parseNumber(locations[1], -1));
          }
        }
        persist.friendList.add(friend);
      }
    }
    return persist;
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem item = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Token");
    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == Menu.NONE) {
      copyToken();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void copyToken() {
    Util.runAsync(new Runnable() {
      @Override public void run() {
        StringBuilder token = new StringBuilder();
        String errMsg = TokenManager.readTokenFromSlowly(token);
        if (TextUtils.isEmpty(token.toString())) {
          Util.toast("error: " + errMsg);
        } else {
          Util.copyToClipboard(TokenManager.packToken(token.toString()));
          Util.toast("Token has been copied");
        }
      }
    });
  }
}
