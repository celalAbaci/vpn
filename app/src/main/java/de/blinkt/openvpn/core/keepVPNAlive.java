package de.blinkt.openvpn.core;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;

import de.blinkt.openvpn.VpnProfile;

public class keepVPNAlive extends JobService {
    private static final int KEEPALIVE_JOB_ID = 456;

    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            PersistableBundle extras = params.getExtras();
            String profileUUID = extras.getString(VpnProfile.EXTRA_PROFILEUUID);

            if (profileUUID != null) {
                VpnProfile profile = ProfileManager.get(this, profileUUID, 0, 0);
                if (profile != null) {
                    // VPN servisini yeniden başlatmayı dene
                    startOpenVPNService(this, profile);
                }
            }
        } catch (Exception e) {
            VpnStatus.logError("KeepVPNAlive Error: " + e.getLocalizedMessage());
        }
        return false; // İş hemen bitti, arka planda devam etmiyor
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false; // Yeniden deneme (Reschedule) yapma
    }

    public static void scheduleKeepVPNAliveJobService(Context context, VpnProfile profile) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return; // Eski cihazlarda JobScheduler yok
        }

        try {
            ComponentName serviceComponent = new ComponentName(context, keepVPNAlive.class);
            JobInfo.Builder builder = new JobInfo.Builder(KEEPALIVE_JOB_ID, serviceComponent);

            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            builder.setPersisted(true); // Cihaz yeniden başlayınca hatırla

            // Profil bilgisini paketle
            PersistableBundle extras = new PersistableBundle();
            extras.putString(VpnProfile.EXTRA_PROFILEUUID, profile.getUUIDString());
            builder.setExtras(extras);

            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.schedule(builder.build());
            }
        } catch (Exception e) {
            VpnStatus.logError("Could not schedule keepalive: " + e.getLocalizedMessage());
        }
    }

    public static void unscheduleKeepVPNAliveJobService(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        try {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.cancel(KEEPALIVE_JOB_ID);
            }
        } catch (Exception e) {
            // Hata yok sayılır
        }
    }

    private void startOpenVPNService(Context context, VpnProfile profile) {
        // LaunchVPN yerine doğrudan servisi başlatıyoruz
        Intent intent = new Intent(context, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        intent.putExtra(VpnProfile.EXTRA_PROFILEUUID, profile.getUUIDString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}