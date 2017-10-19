process.env.SPARKY_LOG = true;

const { FuseBox, Sparky } = require('fuse-box');
const { EnvPlugin, RawPlugin, WebIndexPlugin } = require('fuse-box');

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
projectRoot.build = () => path.join(projectRoot(), 'build');
projectRoot.build.schema = () => path.join(projectRoot.build(), 'schema');
projectRoot.build.frontend = () => path.join(projectRoot.build(), 'frontend');
projectRoot.cache = () => path.join(projectRoot(), '.fusebox');

let production = false;

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

Sparky.task('build:schema', () => new Promise((resolve, reject) => {
    const jsonOptions = {
        spaces: 4
    };

    // add schemas here
    // one entry has the schema subdirectory as key and an array of file names without extension as value
    const schemas = {
        config: ['entrypoints'],
        history: ['change']
    };

    Object.keys(schemas).forEach((type) => schemas[type].forEach((schema) => {
        const out = path.join(projectRoot.build.schema(), type, `${schema}.json`);

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
        });

        renderer.hmr().watch();
    }

    return fuse.run();
});

Sparky.task('clean:build', () => Sparky.src(projectRoot.build()).clean(projectRoot.build()));

Sparky.task('clean:cache', () => Sparky.src(projectRoot.cache()).clean(projectRoot.cache()));

Sparky.task('build:frontend', ['build:frontend:renderer', 'build:frontend:main'], () => {
    return Sparky.src(path.join(projectRoot(), 'package.json')).dest(projectRoot.build());
});

Sparky.task('build:dev', ['clean:build', 'clean:cache', 'build:backend', 'build:schema', 'build:frontend'], () => {});

Sparky.task('set:production', () => production = true);

Sparky.task('build:dist', ['set:production', 'build:dev'], () => {});