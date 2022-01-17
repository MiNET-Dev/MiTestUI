package za.co.megaware.MinetService;
import android.os.Parcel;
import android.os.Parcelable;

public class ByteParceAble implements Parcelable{
    private byte[] _byte;
    public ByteParceAble() {

    }

    public ByteParceAble(Parcel in) {
        readFromParcel(in);
    }

    public byte[] get_byte() {
        return _byte;
    }

    public void set_byte(byte[] _byte) {
        this._byte = _byte;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeInt(_byte.length);
//        dest.writeByteArray(_byte);
        dest.writeByteArray(_byte);
    }

    public void readFromParcel(Parcel in) {
//        _byte = new byte[in.readInt()];
//        in.readByteArray(_byte);
        _byte = in.createByteArray();
    }

//    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
//        public ByteParceAble createFromParcel(Parcel in) {
//            return new ByteParceAble(in);
//        }
//
//        public ByteParceAble[] newArray(int size) {
//            return new ByteParceAble[size];
//        }
//    };

    public static final Parcelable.Creator<ByteParceAble> CREATOR = new Parcelable.Creator<ByteParceAble>() {
        @Override
        public ByteParceAble createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new ByteParceAble(source);
        }
        @Override
        public ByteParceAble[] newArray(int size) {
            // TODO Auto-generated method stub
            return new ByteParceAble[size];
        }
    };

}


