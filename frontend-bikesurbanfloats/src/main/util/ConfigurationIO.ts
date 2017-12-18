import * as AJV from 'ajv';
import { app } from 'electron';
import * as fs from 'fs-extra';
import * as paths from 'path';
import { BaseConfiguration } from '../../shared/configuration';
import { IpcChannel, IpcUtil } from './IpcUtil';

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
            const confIO = create();

            const channels = [
                new IpcChannel('configuration-read', confIO.read()),
                new IpcChannel('configuration-write', confIO.write(path))
            ];

            channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('configuration-close', () => {
                IpcUtil.closeChannels('history-close', ...channels.map((channel) => channel.name));
                this.enableIpc();
            })

            IpcUtil.closeChannels('configuration-init');
        })
    }


}
