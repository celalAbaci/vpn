/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: C:\Users\asusgaming\AppData\Local\Android\Sdk\build-tools\35.0.0\aidl.exe -pC:\Users\asusgaming\AppData\Local\Android\Sdk\platforms\android-35\framework.aidl -oC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\build\generated\aidl_source_output_dir\uiOvpn23Debug\out -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\main\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\ovpn23\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\ui\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\uiOvpn23\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\debug\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\uiOvpn23Debug\aidl -IC:\Users\asusgaming\.gradle\caches\8.13\transforms\d01d6547630dc24d0b5220ff2569aa5f\transformed\core-1.12.0\aidl -IC:\Users\asusgaming\.gradle\caches\8.13\transforms\1bd9c5f03011060a00a35e8866ef1c3c\transformed\versionedparcelable-1.1.1\aidl -dC:\Users\ASUSGA~1\AppData\Local\Temp\aidl16087281451365195914.d C:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\main\aidl\de\blinkt\openvpn\api\IOpenVPNStatusCallback.aidl
 */
package de.blinkt.openvpn.api;
/**
 * Example of a callback interface used by IRemoteService to send
 * synchronous notifications back to its clients.  Note that this is a
 * one-way interface so the server does not block waiting for the client.
 */
public interface IOpenVPNStatusCallback extends android.os.IInterface
{
  /** Default implementation for IOpenVPNStatusCallback. */
  public static class Default implements de.blinkt.openvpn.api.IOpenVPNStatusCallback
  {
    /** Called when the service has a new status for you. */
    @Override public void newStatus(java.lang.String uuid, java.lang.String state, java.lang.String message, java.lang.String level) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements de.blinkt.openvpn.api.IOpenVPNStatusCallback
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an de.blinkt.openvpn.api.IOpenVPNStatusCallback interface,
     * generating a proxy if needed.
     */
    public static de.blinkt.openvpn.api.IOpenVPNStatusCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof de.blinkt.openvpn.api.IOpenVPNStatusCallback))) {
        return ((de.blinkt.openvpn.api.IOpenVPNStatusCallback)iin);
      }
      return new de.blinkt.openvpn.api.IOpenVPNStatusCallback.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      if (code == INTERFACE_TRANSACTION) {
        reply.writeString(descriptor);
        return true;
      }
      switch (code)
      {
        case TRANSACTION_newStatus:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          java.lang.String _arg2;
          _arg2 = data.readString();
          java.lang.String _arg3;
          _arg3 = data.readString();
          this.newStatus(_arg0, _arg1, _arg2, _arg3);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements de.blinkt.openvpn.api.IOpenVPNStatusCallback
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /** Called when the service has a new status for you. */
      @Override public void newStatus(java.lang.String uuid, java.lang.String state, java.lang.String message, java.lang.String level) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(uuid);
          _data.writeString(state);
          _data.writeString(message);
          _data.writeString(level);
          boolean _status = mRemote.transact(Stub.TRANSACTION_newStatus, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_newStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "de.blinkt.openvpn.api.IOpenVPNStatusCallback";
  /** Called when the service has a new status for you. */
  public void newStatus(java.lang.String uuid, java.lang.String state, java.lang.String message, java.lang.String level) throws android.os.RemoteException;
}
