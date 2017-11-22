import { ipcMain, Event } from 'electron';

type SuccessCallback = (data?: any) => Promise<any>;
type ErrorCallback = (error?: Error) => void;

export default class IpcUtil {

    static openChannel(channel: string, onSuccess: SuccessCallback, onError?: ErrorCallback) {
        ipcMain.on(channel, async (event: Event, data?: any) => {
            try {
                event.sender.send(channel, {
                    status: 200,
                    data: await onSuccess(data)
                });
            } catch (error) {
                event.sender.send(channel, {
                    status: 500,
                    data: error
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
