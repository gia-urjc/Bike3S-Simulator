import { Event, ipcMain } from 'electron';

type SuccessCallback = (...data: Array<any>) => Promise<any>;
type ErrorCallback = (error?: Error) => void;

export default class IpcUtil {

    static openChannel(channel: string, onSuccess: SuccessCallback, onError?: ErrorCallback) {
        ipcMain.on(channel, async (event: Event, ...data: Array<any>) => {
            try {
                const responseData = await onSuccess(...data);
                event.sender.send(channel, {
                    status: 200,
                    data: responseData,
                });
            } catch (error) {
                console.log('Error', error);
                event.sender.send(channel, {
                    status: 500,
                    data: {
                        message: error.message,
                        stack: error.stack
                    }
                });
                onError && onError(error);
            }
        });
    }

    static closeChannel(channel: string): void {
        ipcMain.removeAllListeners(channel);
    }

    static closeChannels(...channels: Array<string>): void {
        channels.forEach(this.closeChannel);
    }

}
