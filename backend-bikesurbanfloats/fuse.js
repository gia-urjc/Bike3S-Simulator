process.env.SPARKY_LOG = false;

const { Sparky } = require('fuse-box');

const log = require('fliplog');

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs-extra');

Sparky.task('backend:package', () => new Promise((resolve, reject) => {
    const backendPath = path.resolve(process.cwd(), 'backend-bikesurbanfloats');

    const maven = spawn('mvn', ['package'], {
        cwd: backendPath,
        shell: true // necessary for windows
    });

    log.time().green('start packaging backend jar').echo();

    maven.stdout.pipe(process.stdout);
    maven.stderr.pipe(process.stderr);

    maven.on('error', (error) => {
        log.red(error).echo();
    });

    maven.on('close', (code) => {
        if (code === 0) {
            const targetPath = path.resolve(backendPath, 'target');
            const target = path.resolve(targetPath, fs.readdirSync(targetPath).find((file) => file.endsWith('.jar')));
            const destination = path.resolve(process.cwd(), 'dist', 'resources', 'backend.jar');

            fs.copySync(target, destination);

            log.time().green('finished packaging backend jar').echo();

            resolve();
        } else {
            log.time().red(`maven finished with error code ${code}`).echo();
            reject();
        }
    });
}));

Sparky.task('build:schema', () => new Promise((resolve, reject) => {
    const jsonOptions = {
        spaces: 4
    };

    const schemas = {
        config: ['entrypoints'],
        history: ['change']
    };

    Object.keys(schemas).forEach((type) => schemas[type].forEach((schema) => {
        const out = path.resolve(process.cwd(), 'dist', 'resources', 'schema', type, `${schema}.json`);

        log.time().green(`Writing schema to: ${out}`).echo();

        try {
            fs.outputJsonSync(out, require(`./schema/${type}/${schema}`), jsonOptions);
        } catch (error) {
            log.time().red(`Error while writing json schema: ${error}`).echo();
            reject();
        }
    }));

    resolve();
}));