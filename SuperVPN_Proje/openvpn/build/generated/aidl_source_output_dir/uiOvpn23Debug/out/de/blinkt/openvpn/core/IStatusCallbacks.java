/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: C:\Users\asusgaming\AppData\Local\Android\Sdk\build-tools\35.0.0\aidl.exe -pC:\Users\asusgaming\AppData\Local\Android\Sdk\platforms\android-35\framework.aidl -oC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\build\generated\aidl_source_output_dir\uiOvpn23Debug\out -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\main\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\ovpn23\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\ui\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\uiOvpn23\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\debug\aidl -IC:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\uiOvpn23Debug\aidl -IC:\Users\asusgaming\.gradle\caches\8.13\transforms\d01d6547630dc24d0b5220ff2569aa5f\transformed\core-1.12.0\aidl -IC:\Users\asusgaming\.gradle\caches\8.13\transforms\1bd9c5f03011060a00a35e8866ef1c3c\transformed\versionedparcelable-1.1.1\aidl -dC:\Users\ASUSGA~1\AppData\Local\Temp\aidl5541172161195991000.d C:\Users\asusgaming\OneDrive\Desktop\BBBF\AndroidStudioProjects\StudioProjects\SuperVPN_Proje\openvpn\src\main\aidl\de\blinkt\openvpn\core\IStatusCallbacks.aidl
 */
package de.blinkt.openvpn.core;
/**
 * Used to notify the UI process from the :openvpn service process of changes/event happening in
 * the backend
 */
public interface IStatusCallbacks extends android.os.IInterface
{
  /** Default implementation for IStatusCallbacks. */
  public static class Default implements de.blinkt.openvpn.core.IStatusCallbacks
  {
    /** Called when the service has a new status for you. */
    @Override public void newLogItem(de.blinkt.openvpn.core.LogItem item) throws android.os.RemoteException
    {
    }
    @Override public void updateStateString(java.lang.String state, java.lang.String msg, int resid, de.blinkt.openvpn.core.ConnectionStatus level, android.content.Intent intent) throws android.os.RemoteException
    {
    }
    @Override public void updateByteCount(long inBytes, long outBytes) throws android.os.RemoteException
    {
    }
    @Override public void connectedVPN(java.lang.String uuid) throws android.os.RemoteException
    {
    }
    @Override public void notifyProfileVersionChanged(java.lang.String uuid, int profileVersion) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements de.blinkt.openvpn.core.IStatusCallbacks
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an de.blinkt.openvpn.core.IStatusCallbacks interface,
     * generating a proxy if needed.
     */
    public static de.blinkt.openvpn.core.IStatusCallbacks asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof de.blinkt.openvpn.core.IStatusCallbacks))) {
        return ((de.blinkt.openvpn.core.IStatusCallbacks)iin);
      }
      return new de.blinkt.openvpn.core.IStatusCallbacks.Stub.Proxy(obj);
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
        case TRANSACTION_newLogItem:
        {
          de.blinkt.openvpn.core.LogItem _arg0;
          _arg0 = _Parcel.readTypedObject(data, de.blinkt.openvpn.core.LogItem.CREATOR);
          this.newLogItem(_arg0);
          break;
        }
        case TRANSACTION_updateStateString:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          int _arg2;
          _arg2 = data.readInt();
          de.blinkt.openvpn.core.ConnectionStatus _arg3;
          _arg3 = _Parcel.readTypedObject(data, de.blinkt.openvpn.core.ConnectionStatus.CREATOR);
          android.content.Intent _arg4;
          _arg4 = _Parcel.readTypedObject(data, android.content.Intent.CREATOR);
          this.updateStateString(_arg0, _arg1, _arg2, _arg3, _arg4);
          break;
        }
        case TRANSACTION_updateByteCount:
        {
          long _arg0;
          _arg0 = data.readLong();
          long _arg1;
          _arg1 = data.readLong();
          this.updateByteCount(_arg0, _arg1);
          break;
        }
        case TRANSACTION_connectedVPN:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.connectedVPN(_arg0);
          break;
        }
        case TRANSACTION_notifyProfileVersionChanged:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          int _arg1;
          _arg1 = data.readInt();
          this.notifyProfileVersionChanged(_arg0, _arg1);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements de.blinkt.openvpn.core.IStatusCallbacks
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
      @Override public void newLogItem(de.blinkt.openvpn.core.LogItem item) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, item, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_newLogItem, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void updateStateString(java.lang.String state, java.lang.String msg, int resid, de.blinkt.openvpn.core.ConnectionStatus level, android.content.Intent intent) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(state);
          _data.writeString(msg);
          _data.writeInt(resid);
          _Parcel.writeTypedObject(_data, level, 0);
          _Parcel.writeTypedObject(_data, intent, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_updateStateString, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void updateByteCount(long inBytes, long outBytes) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(inBytes);
          _data.writeLong(outBytes);
          boolean _status = mRemote.transact(Stub.TRANSACTION_updateByteCount, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void connectedVPN(java.lang.String uuid) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(uuid);
          boolean _status = mRemote.transact(Stub.TRANSACTION_connectedVPN, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void notifyProfileVersionChanged(java.lang.String uuid, int profileVersion) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(uuid);
          _data.writeInt(profileVersion);
          boolean _status = mRemote.transact(Stub.TRANSACTION_notifyProfileVersionChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_newLogItem = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_updateStateString = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_updateByteCount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_connectedVPN = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_notifyProfileVersionChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "de.blinkt.openvpn.core.IStatusCallbacks";
  /** Called when the service has a new status for you. */
  public void newLogItem(de.blinkt.openvpn.core.LogItem item) throws android.os.RemoteException;
  public void updateStateString(java.lang.String state, java.lang.String msg, int resid, de.blinkt.openvpn.core.ConnectionStatus level, android.content.Intent intent) throws android.os.RemoteException;
  public void updateByteCount(long inBytes, long outBytes) throws android.os.RemoteException;
  public void connectedVPN(java.lang.String uuid) throws android.os.RemoteException;
  public void notifyProfileVersionChanged(java.lang.String uuid, int profileVersion) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}
