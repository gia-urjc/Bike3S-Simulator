import ajv = require('ajv');
import * as fs from 'fs-extra';
import * as AJV from 'ajv';
import { app } from 'electron';
import * as paths from 'path';
import { BaseConfiguration } from '../../shared/configuration';

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


}
