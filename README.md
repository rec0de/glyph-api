# Glyph API

_How you could use the glyph lights on your nothing phone, and why you can't, and how you maybe could anyway?_

It seems there is not a lot of information out there on how the glyph lights on a nothing phone (i'll be talking about the phone 2, but this should largely also apply to the phone 1) work and how they can be controlled by third-party, i.e. non-root apps. We just know that it has to work _somehow_ because the Glyph Composer app is able to do so. So I did a little reversing to shed some light:

# Theory

Internally, the lights are controlled using the [Android Hardware Lights Service](https://developer.android.com/reference/android/hardware/lights/LightsManager). Using this service requires the `CONTROL_DEVICE_LIGHTS_PERMISSION`, which is only granted to system apps. So that's a dead end.

So how does the glyph composer do it?

Looking into a decompiled APK, we can find an interesting permission:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:versionCode="1030000"
    android:versionName="1.3.0"
    android:compileSdkVersion="33"
    android:compileSdkVersionCodename="13"
    package="com.nothing.glyph.composer"
    platformBuildVersionCode="33"
    platformBuildVersionName="13">

    ...

    <uses-permission
        android:name="com.nothing.ketchum.permission.ENABLE" />
    ...

</manifest>
```

Diving a bit deeper into the decompiled code, we find that the app binds to a promising looking service:

```java
public final void b() {
  Object m7;
        r3.a aVar = this.f2457d;
        aVar.getClass();
        try {
            if (aVar.f6404b == 0) {
                s3.a b7 = aVar.b();
                b7.getClass();
                Intent intent = new Intent();
                intent.setPackage("com.nothing.thirdparty");
                intent.setAction("com.nothing.thirdparty.bind_glyphservice");
                intent.setComponent(new ComponentName("com.nothing.thirdparty", "com.nothing.thirdparty.GlyphService"));
                b7.f6730a.bindService(intent, b7.f6731b, 1);
                Log.i("GlyphComposer_GlyphManagerCompat", "init");
            }
            int i7 = aVar.f6404b;
            aVar.f6404b = i7 + 1;
            m7 = Integer.valueOf(i7);
        } catch (Throwable th) {
            m7 = e1.m(th);
        }
        Throwable a7 = h4.d.a(m7);
        if (a7 != null) {
            String str = "init error:" + a7;
            h.e(str, "message");
            Log.e("GlyphComposer_GlyphManagerCompat", str);
        }
}
```

The interface for this service looks rather simple, and after a little reversing it boils down to this:

```kotlin
interface GlyphInterface : IInterface {
    fun setFrameColors(iArr: IntArray)
    fun closeSession()
    fun openSession()
    fun register(str: String): Boolean
}
```

Looks pretty straightforward, right? You connect to the service, call `openSession` and then control the lights using `setFrameColor`, which I strongly suspect takes 33 brightness values for each of the addressable zones --- I'll put a very minimal example on how you would use this in an app at the bottom.

# Obstacles

There's just one problem: Nothing doesn't want you to do that. Let's have a look at the `com.nothing.thirdparty` package to see what's going on. Here's a condensed version of the `` file that is essentially the 'other end' of the `GlyphInterface` we've seen in the composer.

```java
public class GlyphService extends Service {
    private static final boolean DBG = Def.DBG;
    private Context mContext;
    private LightsManager mLightsManager;
    private AuthController mAuth = null;
    private GlyphReceiver mGlyphReciever = null;
    private GlyphAdapter mAdapter = null;
    private AuthController.Callback mAuthCallback = null;
    private GlyphReceiver.Callback mGlyphReceiverCallback = null;
    private String mCurrentFocusPkg = null;
    private HashMap<Integer, LightsManager.LightsSession> mSessionMap = new HashMap<>();
    private HashMap<Integer, String> mUidPkgMap = new HashMap<>();
    private IGlyphService.Stub mStub = new IGlyphService.Stub() { 

        public boolean register(String str) throws RemoteException {
            if (GlyphService.DBG) {
                Log.d("GlyphService", "register");
            }
            return GlyphService.this.mAuth.register(Utils.getCallingPackageName(GlyphService.this.mContext), str, Utils.getCallingUid());
        }

        public void openSession() throws RemoteException {
            boolean allowInBackground;
            synchronized (GlyphService.this.mSessionMap) {
                String callingPackageName = Utils.getCallingPackageName(GlyphService.this.mContext);
                int callingUid = Utils.getCallingUid();
                int i = 115;
                boolean authorized = true;
                if ("com.nothing.glyph.composer".equals(callingPackageName) && Utils.checkFingerprint(GlyphService.this.mContext, callingPackageName)) {
                    GlyphService.this.mAuth.addAlreadyAuth(callingPackageName, callingUid);
                    i = 110;
                    allowInBackground = true;
                } else {
                    allowInBackground = false;
                }
                if (callingUid == 1000) {
                    allowInBackground = true;
                }
                if (!GlyphService.this.mUidPkgMap.containsKey(callingUid)) {
                    GlyphService.this.mUidPkgMap.put(callingUid, callingPackageName);
                }
                if (GlyphService.this.mAuth.checkAlreadyAuth(GlyphService.this.mUidPkgMap.get(callingUid))) {
                    if (!GlyphService.this.mAuth.checkForeground(callingPackageName)) {
                        authorized = allowInBackground;
                    }
                    if (authorized) {
                        if (GlyphService.this.mSessionMap.get(i) == null) {
                            GlyphService.this.mSessionMap.put(i, GlyphService.this.mLightsManager.openSession());
                            if (GlyphService.DBG) {
                                Log.d("GlyphService", "openSession:" + callingPackageName);
                            }
                        } else if (GlyphService.DBG) {
                            Log.d("GlyphService", "already openSession");
                        }
                    } else {
                        Log.e("GlyphService", "Fail to connect.");
                    }
                }
            }
        }
        
        public void closeSession() throws RemoteException { ... }

        public void setFrameColors(int[] iArr) throws RemoteException {
            String str = GlyphService.this.mUidPkgMap.getOrDefault(Utils.getCallingUid(), null);
            GlyphService.this.mCurrentFocusPkg = str;
            LightsManager.LightsSession lightsSession = GlyphService.this.mSessionMap.getOrDefault(Integer.valueOf("com.nothing.glyph.composer".equals(str) ? 110 : 115), null);
            if (str == null) {
                Log.e("GlyphService", "pkg is null");
            } else if (lightsSession == null) {
                Log.e("GlyphService", "session is null");
            } else if (!GlyphService.this.mAuth.checkAlreadyAuth(str)) {
                Log.e("GlyphService", "Non register");
            } else if (!GlyphService.this.mAuth.checkForeground(str)) {
                GlyphService.this.resetFrameColor(lightsSession);
                Log.e("GlyphService", str + " is not foreground");
            } else {
                GlyphService.this.setFrameColorsInner(lightsSession, iArr);
            }
        }
    };

    public void setFrameColorsInner(LightsManager.LightsSession lightsSession, int[] iArr) { ... }
}
```

See how we have to get through all these if statements in `openSession`? Let's break that down: Essentially, we have a call to `mAtuh.checkAlreadyAuth(ourPackageName)` that has to return true for us to get anywhere. Also, the glyph composer and system apps (pid 1000) explicitly get some special treatment that allows them to run in the background. Similar rules apply in `setFrameColors` --- - we have to be authenticated and running in the foreground (unless we have special privileges).

The way we authenticate ourselves seems to be the `register(str)` method, but let's look into the `AuthController.java` file to see how exactly:

```java
public boolean register(String packageName, String apikey, int pid) {
    if (packageName == null || apikey == null || "".equals(apikey)) {
        if (Def.DBG) {
            Log.d("AuthController", "pkg:" + packageName + ", uid:" + pid);
        }
        return false;
    } else if (pid == 1000) {
        if (Def.DBG) {
            Log.d("AuthController", "register(), system uid");
        }
        addAlreadyAuth(packageName, pid, 2);
        return true;
    } else {
        AuthApp authApp = this.mAuthMap.get(packageName);
        if (authApp == null) {
            if (Def.DBG) {
                Log.d("AuthController", "Wrong pkg");
            }
            return false;
        }
        authApp.setUid(pid);
        return authApp.checkAuth(apikey, Utils.getCertificateFingerprint(this.mContext, packageName));
    }
}
```

So system apps (pid 1000) are always accepted, while other apps have to supply an API key. Their package name also has to be present in the `mAuthMap` map, which is provided in JSON format over-the-air by nothing (details in `RemoteConfigController.java`). The `authApp.checkAuth` method just checks that both the api key and the "sign key", a SHA1 hash of the calling app's signing key match the expected values provided in the JSON auth map. Here's `Utils.getCertificateFingerprint`:

```java
public static String getCertificateFingerprint(Context context, String str) {
    String str2 = "";
    try {
        byte[] byteArray = context.getPackageManager().getPackageInfo(str, PackageManager.GET_SIGNATURES).signatures[0].toByteArray();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        messageDigest.update(byteArray);
        for (byte b : messageDigest.digest()) {
            String num = Integer.toString(b & 255, 16);
            if (num.length() == 1) {
                str2 = str2 + "0";
            }
            str2 = str2 + num;
        }
        return str2.toUpperCase();
    } catch (PackageManager.NameNotFoundException e) {
        Log.e("ThirdParty:Utils", e.getMessage(), e);
        return str2;
    } catch (NoSuchAlgorithmException e2) {
        Log.e("ThirdParty:Utils", e2.getMessage(), e2);
        return str2;
    }
}
```

So things are looking rather grim --- short of begging nothing to give you an API key specifically tied to your app signing credentials, there's not really a way to make this work. And you'll need an extra special key if you want to do things in the background, which is probably even more unrealistic to obtain. You can't extract or steal credentials from other apps, and you can't even patch existing apps that have valid credentials, because either of those options break the signature fingerprint.

# Praxis?

So what _can_ you do?

Well, there's one interesting quirk: Once an app has registered, it never loses that authentication status, at least from what I can tell. With no real understanding of android service lifecycles I'm really just conjecturing out of my league here, but I think it _might_ be possible to 

1. install an authenticated app (i.e. the glyph composer)
2. let that app register using its credentials
3. uninstall the app
4. install your own app, using an identical package name
5. open a session without registering

In this case, the `checkAlreadyAuth` check should succeed because it only uses your package name to look up authentication status. Note that I tried this approach briefly and couldn't get it to work, but that might just be my lacking android skills.

But there's one other and potentially even more powerful trick: Recall the curious line in `openSession` that allows the composer to authenticate without ever calling `register` with an API key:

```java
if ("com.nothing.glyph.composer".equals(callingPackageName) && Utils.checkFingerprint(GlyphService.this.mContext, callingPackageName)) {
    GlyphService.this.mAuth.addAlreadyAuth(callingPackageName, callingUid);
    ...
}
```

It turns out that bizarrely, the implementation of `checkFingerprint` looks like this:

```java
public static boolean checkFingerprint(Context context, String str) {
        return getCertificateFingerprint(context, str).contains("95E1F157FE98518");
    }
```

See the issue? Not only does it only check for 60 of the 160 bits in the SHA1 hash of the signing key, it also accepts any fingerprint that has these 60 bits **in any position** (aligned to 4bit). This means that it is _very theoretically_ feasible to brute force an android signing key whose SHA1 hash contains this magic substring, which would allow you to impersonate the glyph composer and use the lights as you please.

My combinatorics are a bit rusty but if my math is correct about 1 in 2^55 keys should have this magic property --- that's very very rare, but not completely out there, given that people have been brute-forcing 56bit [DES](https://en.wikipedia.org/wiki/Data_Encryption_Standard) keys successfully many years back, and high-end GPUs seem to be capable of doing so in a handful of days.

Is that worth it for a few blinky lights? I don't know. I guess here's to hoping that nothing will open up the API eventually.

# Notes

It appears that while the online-config API key distribution thing is fully in place, the glyph composer does not use it at all and is rather patched in to receive similar treatment to a system app - it never calls `register` at all. Curiously, this means that if you were to steal the composer package name, you'd probably lose background privileges because the composer does not technically have the correct permission scope for that and your forged packet would fail the fingerprint check in `openSession`. Now that I think of it, the composer probably doesn't really have these privileges in the first place, since it would fail the foreground check in `setFrameColors`.
