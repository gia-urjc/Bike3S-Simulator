import {DataGenerator} from './dataAnalysis/analysis/generators/DataGenerator';
import * as program from 'commander';

namespace Main {

    export async function analysis(historyPath: string, csvPath: string, schemaPath: string): Promise<DataGenerator> {
        try {
            return await DataGenerator.generate(historyPath, csvPath, schemaPath);
        }
        catch(error) {
            console.log('Error: ', error);
            throw Error(error);
        }
    }

}

program
    .version('0.0.1')
    .description('Analyses data from historics of the bike simulator')
    .command('analyse')
    .option('-h, --history [history]', 'history directory')
    .option('-s, --schema [schema]', 'Json schema directory for validations')
    .option('-c, --csv [csv]', 'Csv path for results')
    .action((options: any) => {
        console.log(options.history);
        Main.analysis(options.history, options.csv, options.schema).then(() => {
            console.log("Calculated");
        });

    });

program.parse(process.argv);