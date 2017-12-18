import * as AJV from 'ajv';
import { app } from 'electron';
import * as fs from 'fs-extra';
import * as paths from 'path';
import { BaseConfiguration } from '../../shared/configuration';
import { IpcUtil } from './index';

class Channel {
    constructor(public name: string, public callback: (data?: any) => Promise<any>) {}
}

export default class ConfigurationIO {

    private static ajv = new AJV({ allErrors: true });
    private static entityFileSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/config.json'));

    static create(): ConfigurationIO {
        let writer = new ConfigurationIO();
        return writer;
    }

    public async read(path: string): Promise<BaseConfiguration> {
        let plainConfiguration: BaseConfiguration = await fs.readJSON(path);
        let valid = ConfigurationIO.ajv.validate(ConfigurationIO.entityFileSchema, plainConfiguration);
        if (!valid) {
            throw new Error(ConfigurationIO.ajv.errorsText());
        }
        let baseConfiguration: BaseConfiguration = plainConfiguration;
        return baseConfiguration;
    }

    public write(conf: BaseConfiguration): boolean {
        let valid = ConfigurationIO.ajv.validate(ConfigurationIO.entityFileSchema, conf);
        if (!valid) {
            throw new Error(ConfigurationIO.ajv.errorsText());
        }
        return true;
    }

    public enableIpc(): void {
        IpcUtil.openChannel('configuration-init', async (path: string) => {
            const confIO = ConfigurationIO.create();

            const channels = [
                new Channel('configuration-read', async () => await confIO.read(path)),
                new Channel('configuration-write', async (configuration) => await confIO.write(configuration)),
            ];

            channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('configuration-close', async () => {
                IpcUtil.closeChannels('configuration-close', ...channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('configuration-init');
        })
    }


}
