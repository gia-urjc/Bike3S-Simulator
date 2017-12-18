import ajv = require('ajv');
import * as fs from 'fs-extra';
import * as AJV from 'ajv';
import { app } from 'electron';
import * as paths from 'path';
import { BaseConfiguration } from '../../shared/configuration';

export default class ConfigurationWriter {

    private static ajv = new AJV({ allErrors: true });
    private static entityFileSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/config.json'));

    private configJson: any;

    static async create(path: string): Promise<ConfigurationWriter> {
        let writer = new ConfigurationWriter();
        writer.configJson = await fs.readJSON(path);
        return writer;
    }

    public readAndValidate(): BaseConfiguration {
        let valid = ConfigurationWriter.ajv.validate(ConfigurationWriter.entityFileSchema, this.configJson);
        if (!valid) {
            throw new Error(ConfigurationWriter.ajv.errorsText());
        }
        let baseConfiguration: BaseConfiguration = this.configJson;
        return baseConfiguration;
    }




}
