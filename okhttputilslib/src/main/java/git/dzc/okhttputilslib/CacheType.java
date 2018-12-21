package git.dzc.okhttputilslib;

import android.support.annotation.IntDef;

/**
 * Created by dzc on 15/12/5.
 */
@IntDef({CacheType.ONLY_NETWORK,CacheType.ONLY_CACHED,CacheType.CACHED_ELSE_NETWORK,CacheType.NETWORK_ELSE_CACHED,CacheType.UPDATE_FILE})
public @interface CacheType {
    int ONLY_NETWORK = 0;
    int ONLY_CACHED = 1;
    int CACHED_ELSE_NETWORK =2;
    int NETWORK_ELSE_CACHED = 3;
    int UPDATE_FILE=4;

}
