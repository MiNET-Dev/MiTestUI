// IMainService.aidl
package za.co.megaware.MinetService;

// Declare any non-default types here with import statements
import za.co.megaware.MinetService.IByteParceAble;

interface IMainService {
    /**
         * Demonstrates some basic types that you can use as parameters
         * and return values in AIDL.
         */
        //Device IMEI

        void Sync();

        String GetDeviceIMEI();

        byte[] GetAllDeviceInfo();

        int GetLocationFix(out double[] latlon);

        int[] GetHealthStatus();

        boolean[] GetTamperStatus();

        int GetPrinterStates();

        int PrintFunction(in ByteParceAble data,int feedlines,boolean cutpartial, in String printID); // TODO: ADD THESE CHANGES

        int WriteMifare(int readno,in ByteParceAble UUID,in ByteParceAble data,byte notify);

        void SetInternalAntenna();

        void SetExternalAntenna();

        int WriteRawCommand(byte id,byte cmd,in ByteParceAble rawpayload);

        void ActivateNFCReader(int readerNo);

        void DeactivateNFCReader(int readerNo);

        void WriteJSONMiFARE(in String JSON, in ByteParceAble _UUID, int readno);

        String GetServiceModuleVersion();

        void SetGPSUpdateInterval(in int seconds);

        int GetGPSUpdateInterval();

        void ConfigureBMP(in int index, String filePath, String fileName);

        void DisplayExternalReader(in String validatorState, in int backgroundIndex, in int soundIndex, in int externalReaderIndex);

        void DisplayExternalReaderOverloaded(in int soundIndex, in String msgLineOne, in String msgLineTwo, in String msgLineThree, in String msgLineFour, in String msgLineFive, in String blinkColor, in String blinkIterations, in int overlayIndex, in int externalReaderIndex);

        void UpgradeFirmware(String filePathToFirmware);

        void ConfigureAudio(in int index, String filePath, String fileName);

        void DoBlink(in int readNo, in String color, in int iterations);

        /**
         * This Function will make all LED's turn into the specified color
         * Has Support for Multiple External Readers
         * To turn the LED's off using this function, you will provide a String value of "off" for the @param Color
         * @param color String Value representing the color that will be turned on eg. "Orange"
         * @throws RemoteException
         */
        void DoBlinkStatic(in String color);

        void UpdateMifareKey(in String JSON);

        void PlaySound();

        int NFCTroubleShootStatus(in int NFCReaderNumber);

        int PrinterTroubleShootStatus();

        int MotherboardTroubleShootStatus();

        int ExternalTroubleShootStatus(in int ExternalReaderNumber);

        int GPSTroubleShootStatus();

        String GetDeviceMACAddress();

        String GetDeviceIPAddress();

        void GetBMPIndexes();

        void GetWAVIndexes();
}
