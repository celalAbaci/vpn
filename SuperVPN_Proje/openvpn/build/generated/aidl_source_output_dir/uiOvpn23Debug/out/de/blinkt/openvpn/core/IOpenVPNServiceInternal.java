/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: C:\Users\asusgaming\AppData\Local\Android\Sdk\build-tools\35.0.0\aidl.exe -pC:\Users\asusgaming\AppData\Local\Android\Sdk\platforms\android-35\framework.aidl -oC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\build\generated\aidl_source_output_dir\uiOvpn23Debug\out -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\main\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\ovpn23\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\ui\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\uiOvpn23\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\debug\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\uiOvpn23Debug\aidl -IC:\Users\asusgaming\.gradle\caches\8.13\transforms\d01d6547630dc24d0b5220ff2569aa5f\transformed\core-1.12.0\aidl -IC:\Users\asusgaming\.gradle\caches\8.13\transforms\1bd9c5f03011060a00a35e8866ef1c3c\transformed\versionedparcelable-1.1.1\aidl -dC:\Users\ASUSGA~1\AppData\Local\Temp\aidl3945316713060653548.d C:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\main\aidl\de\blinkt\openvpn\core\IOpenVPNServiceInternal.aidl
 */
package de.blinkt.openvpn.core;
/** Created by arne on 15.11.16. */
public interface IOpenVPNServiceInternal extends android.os.IInterface
{
  /** Default implementation for IOpenVPNServiceInternal. */
  public static class Default implements de.blinkt.openvpn.core.IOpenVPNServiceInternal
  {
    @Override public boolean protect(int fd) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void userPause(boolean b) throws android.os.RemoteException
    {
    }
    /**
     * @param replaceConnection True if the VPN is connected by a new connection.
     * @return true if there was a process that has been send a stop signal
     */
    @Override public boolean stopVPN(boolean replaceConnection) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void addAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException
    {
    }
    @Override public boolean isAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void challengeResponse(java.lang.String repsonse) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements de.blinkt.openvpn.core.IOpenVPNServiceInternal
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an de.blinkt.openvpn.core.IOpenVPNServiceInternal interface,
     * generating a proxy if needed.
     */
    public static de.blinkt.openvpn.core.IOpenVPNServiceInternal asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof de.blinkt.openvpn.core.IOpenVPNServiceInternal))) {
        return ((de.blinkt.openvpn.core.IOpenVPNServiceInternal)iin);
      }
      return new de.blinkt.openvpn.core.IOpenVPNServiceInternal.Stub.Proxy(obj);
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
        case TRANSACTION_protect:
        {
          int _arg0;
          _arg0 = data.readInt();
          boolean _result = this.protect(_arg0);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_userPause:
        {
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          this.userPause(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_stopVPN:
        {
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          boolean _result = this.stopVPN(_arg0);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_addAllowedExternalApp:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.addAllowedExternalApp(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_isAllowedExternalApp:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          boolean _result = this.isAllowedExternalApp(_arg0);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_challengeResponse:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.challengeResponse(_arg0);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements de.blinkt.openvpn.core.IOpenVPNServiceInternal
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
      @Override public boolean protect(int fd) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(fd);
          boolean _status = mRemote.transact(Stub.TRANSACTION_protect, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void userPause(boolean b) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((b)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_userPause, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
       * @param replaceConnection True if the VPN is connected by a new connection.
       * @return true if there was a process that has been send a stop signal
       */
      @Override public boolean stopVPN(boolean replaceConnection) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((replaceConnection)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopVPN, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void addAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(packagename);
          boolean _status = mRemote.transact(Stub.TRANSACTION_addAllowedExternalApp, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public boolean isAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(packagename);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isAllowedExternalApp, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void challengeResponse(java.lang.String repsonse) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(repsonse);
          boolean _status = mRemote.transact(Stub.TRANSACTION_challengeResponse, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_protect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_userPause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_stopVPN = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_addAllowedExternalApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_isAllowedExternalApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_challengeResponse = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "de.blinkt.openvpn.core.IOpenVPNServiceInternal";
  public boolean protect(int fd) throws android.os.RemoteException;
  public void userPause(boolean b) throws android.os.RemoteException;
  /**
   * @param replaceConnection True if the VPN is connected by a new connection.
   * @return true if there was a process that has been send a stop signal
   */
  public boolean stopVPN(boolean replaceConnection) throws android.os.RemoteException;
  public void addAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException;
  public boolean isAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException;
  public void challengeResponse(java.lang.String repsonse) throws android.os.RemoteException;
}
