package org.oucho.radio2.net;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import org.oucho.radio2.PlayerService;
import org.oucho.radio2.utils.Counter;
import org.oucho.radio2.utils.State;

public class Connectivity extends BroadcastReceiver {

   private static ConnectivityManager connectivity = null;

   private Context context = null;
   private PlayerService playerService = null;
   private static final int TYPE_NONE = -1;

   private static int previous_type = TYPE_NONE;

   private Handler handler;

   public Connectivity(Context a_context, PlayerService a_player) {

      context = a_context;
      playerService = a_player;

      initConnectivity(context);
      context.registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
   }

   static private void initConnectivity(Context context) {

      if ( connectivity == null )
         connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if ( connectivity != null )
         previous_type = getType();
   }

   public void destroy() {

      context.unregisterReceiver(this);
   }

   static private int getType() {

      return getType(null);
   }

   static private int getType(Intent intent) {

      if (connectivity == null)
         return TYPE_NONE;

      if ( intent != null && intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) )
         return TYPE_NONE;

      NetworkInfo network = connectivity.getActiveNetworkInfo();
      if ( network != null && network.isConnected() ) {

         int type = network.getType();
         switch (type) {
            // These cases all fall through.
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_WIMAX:
               if ( network.getState() == NetworkInfo.State.CONNECTED )
                  return type;

             default:
                 break;
         }
      }

      return TYPE_NONE;
   }

   public static boolean onWifi() {

      return previous_type == ConnectivityManager.TYPE_WIFI;
   }

   static public boolean isConnected(Context context) {

       initConnectivity(context);

      return (getType() != TYPE_NONE);
   }



   @Override
   public void onReceive(Context context, Intent intent) {

      int type = getType(intent);
      int then = 0;

      boolean want_network_playing = State.isWantPlaying() && playerService.isNetworkUrl();

      if ( type == TYPE_NONE && previous_type != TYPE_NONE && want_network_playing )
         dropped_connection();


      if ( previous_type == TYPE_NONE && type != previous_type && Counter.still(then) )
         restart();

      if ( previous_type != TYPE_NONE && type != TYPE_NONE && type != previous_type && want_network_playing )
         restart();

      previous_type = type;
   }





   public void dropped_connection() {

      Log.d("Connectivity", "dropped_connection(), perte de connexion");

      State.setState(context, State.STATE_DISCONNECTED, true);


      handler = new Handler();
      handler.postDelayed(new Runnable() {

         @SuppressLint("SetTextI18n")
         public void run() {

            if (State.isOnline(context)) {

               Log.d("Connectivity", "dropped_connection(), isOnline");

               Intent player = new Intent(context, PlayerService.class);
               player.putExtra("action", "stop");
               context.startService(player);

               Intent player2 = new Intent(context, PlayerService.class);
               player2.putExtra("action", "play");
               context.startService(player2);

               reconnect(5000);

            } else {

               Intent player3 = new Intent(context, PlayerService.class);
               player3.putExtra("action", "stop");
               context.startService(player3);
            }

         }
      }, 2000);

   }


 private void reconnect(int delay) {

    handler = new Handler();
    handler.postDelayed(new Runnable() {

       @SuppressLint("SetTextI18n")
       public void run() {

          Log.d("Connectivity", "reconnect(int delay), reconnexion x seconde");

          if (!State.isPlaying()) {

             Intent player2 = new Intent(context, PlayerService.class);
             player2.putExtra("action", "play");
             context.startService(player2);
          }
       }

    }, delay);

 }


   private void restart() {

      Log.d("Connectivity", "restart");

      Intent playerS = new Intent(context, PlayerService.class);
      playerS.putExtra("action", "stop");
      context.startService(playerS);

      Intent playerP = new Intent(context, PlayerService.class);
      playerP.putExtra("action", "play");
      context.startService(playerP);

   }
}

