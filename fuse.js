process.env.SPARKY_LOG = false;

const { FuseBox, Sparky } = require('fuse-box');
const { EnvPlugin, CSSPlugin, CSSResourcePlugin, RawPlugin, WebIndexPlugin } = require('fuse-box');

const log = require('fliplog');
const express = require('express');

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs-extra');

const projectRoot = () => process.cwd();
projectRoot.backend = () => path.join(projectRoot(), 'backend-bikesurbanfloats');
projectRoot.backend.mavenTarget = () => path.join(projectRoot.backend(), 'target');
projectRoot.frontend = () => path.join(projectRoot(), 'frontend-bikesurbanfloats');
projectRoot.frontend.main = () => path.join(projectRoot.frontend(), 'src/main');
projectRoot.frontend.renderer = () => path.join(projectRoot.frontend(), 'src/renderer');
projectRoot.schema = () => path.join(projectRoot(), 'schema');
projectRoot.jsonschemaValidator = () => path.join(projectRoot(), 'jsonschema-validator/src');
projectRoot.build = () => path.join(projectRoot(), 'build');
projectRoot.build.schema = () => path.join(projectRoot.build(), 'schema');
projectRoot.build.frontend = () => path.join(projectRoot.build(), 'frontend');
projectRoot.build.jsonschemaValidator = () => path.join(projectRoot.build(), 'jsonschema-validator');
projectRoot.fuseCache = () => path.join(projectRoot(), '.fusebox');
projectRoot.schemaCache = () => path.join(projectRoot(), '.schema');

let production = false;

let schemaBuildPath = projectRoot.build.schema();

Sparky.task('build:backend', () => new Promise((resolve, reject) => {
    const maven = spawn('mvn', ['clean', 'package'], {
        cwd: projectRoot.backend(),
        shell: true, // necessary for windows
        stdio: 'inherit' // pipe to calling process
    });

    log.time().green('start packaging backend jar').echo();

    maven.on('error', (error) => {
        log.red(error).echo();
    });

    maven.on('close', (code) => {
        if (code === 0) {
            const jar = fs.readdirSync(projectRoot.backend.mavenTarget()).find((file) => file.endsWith('.jar'));
            const target = path.join(projectRoot.backend.mavenTarget(), jar);
            const destination = path.join(projectRoot.build(), 'backend.jar');

            fs.copySync(target, destination);

            log.time().green('finished packaging backend jar').echo();

            resolve();
        } else {
            log.time().red(`maven finished with error code ${code}`).echo();
            reject();
        }
    });
}));

Sparky.task('build:schema', ['clean:cache:schema'], () => new Promise((resolve, reject) => {
    const tsc = spawn(path.join(projectRoot(), 'node_modules/.bin/tsc'), [], {
        cwd: projectRoot.schema(),
        shell: true,
        stdio: 'inherit'
    });

    tsc.on('error', (error) => {
        log.red(error).echo();
    });

    tsc.on('close', (code) => {
        if (code === 0) {
            log.time().green('compiling schemas').echo();

            fs.readdirSync(projectRoot.schemaCache()).filter((file) => file.endsWith('.js')).forEach((file) => {
                const schema = require(path.join(projectRoot.schemaCache(), file)).default;
                const out = path.join(schemaBuildPath, `${file.slice(0, -3)}.json`);

                schema.errors.forEach((error) => {
                    log.red(error).echo();
                });

                schema.write(out);

                log.time().green(`written schema to ${out}`).echo();
            });

            resolve();
        } else {
            log.time().red(`tsc finished with error code ${code}`).echo();
            reject();
        }
    });
}));

Sparky.task('build:jsonschema-validator', () => {
    const fuse = FuseBox.init({
        homeDir: projectRoot.jsonschemaValidator(),
        output: path.join(projectRoot.build.jsonschemaValidator(), '$name.js')
    });
    
    fuse.bundle("jsonschema-validator.js").instructions(`>index.ts`);
    
    fuse.run();
});

Sparky.task('build:frontend:main', () => {
    const fuse = FuseBox.init({
        homeDir: projectRoot.frontend.main(),
        output: path.join(projectRoot.build.frontend(), '$name.js'),
        target: 'server',
        experimentalFeatures: true,
        cache: !production,
        plugins: [
            EnvPlugin({ target: production ? 'production' : 'development' }),
        ]
    });

    const main = fuse.bundle('main').instructions('> [main.ts]');

    if (!production) {
        main.watch();
        return fuse.run().then(() => {
            const electron = spawn('npm', ['run', 'start:electron'], {
                cwd: projectRoot(),
                shell: true, // necessary on windows
                stdio: 'inherit' // pipe to calling process
            });
        });
    }

    return fuse.run();
});

Sparky.task('build:frontend:renderer', () => {
    const fuse = FuseBox.init({
        homeDir: projectRoot.frontend.renderer(),
        output: path.join(projectRoot.build.frontend(), '$name.js'),
        sourceMaps: {
            project: !production,
            vendor: false, // vendor sourcemaps take very long to generate
        },
        target: 'electron',
        experimentalFeatures: true,
        cache: !production,
        plugins: [
            EnvPlugin({ target: production ? 'production' : 'development' }),
            ['*.component.html', RawPlugin()],
            ['*.component.css', RawPlugin()],
            [CSSResourcePlugin({ inline: true }), CSSPlugin()],
            WebIndexPlugin({
                template: path.join(projectRoot.frontend.renderer(), 'index.html'),
                path: '.'
            })
        ]
    });

    const vendor = fuse.bundle('vendor').instructions('~ renderer.ts');
    const renderer = fuse.bundle('renderer').instructions('!> [renderer.ts]');

    if (!production) {
        fuse.dev({ root: false }, (server) => {
            const app = server.httpServer.app;
            app.use('/frontend/', express.static(projectRoot.build.frontend()));
            app.get('*', (request, response) => {
                response.send(path.join(projectRoot.build.frontend(), 'index.html'));
            });
            // TODO: make the server close on electron window close
        });

        renderer.hmr().watch();
    }

    return fuse.run();
});

Sparky.task('clean:build', () => Sparky.src(projectRoot.build()).clean(projectRoot.build()));

Sparky.task('clean:cache:fuse', () => Sparky.src(projectRoot.fuseCache()).clean(projectRoot.fuseCache()));
Sparky.task('clean:cache:schema', () => Sparky.src(projectRoot.schemaCache()).clean(projectRoot.schemaCache()));

Sparky.task('clean:cache', ['clean:cache:fuse', 'clean:cache:schema'], () => {});

Sparky.task('build:frontend', ['build:frontend:renderer', 'build:frontend:main'], () => {
    return Sparky.src(path.join(projectRoot(), 'package.json')).dest(projectRoot.build());
});

Sparky.task('build:dev', ['clean:build', 'clean:cache', 'build:backend', 'build:schema', 'build:frontend', 'build:jsonschema-validator'], () => {});

Sparky.task('build:dist', () => {
    production = true;
    return Sparky.start('build:dev');
});

Sparky.task('build:schema:forBackend', () => {
    schemaBuildPath = path.join(projectRoot.backend(), 'schema');
    return Sparky.start('build:schema');
});