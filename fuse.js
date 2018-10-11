process.env.SPARKY_LOG = false;
const { FuseBox, Sparky } = require('fuse-box');
const { EnvPlugin, CSSPlugin, CSSResourcePlugin, RawPlugin, WebIndexPlugin, JSONPlugin, QuantumPlugin } = require('fuse-box');
const log = require('fliplog');
const express = require('express');
const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs-extra');
const request = require('request');
const progress = require('request-progress');

const projectRoot = () => process.cwd();

projectRoot.backendRoot = () => path.join(projectRoot(), 'backend-bikesurbanfleets');
projectRoot.configurationFiles = () => path.join(projectRoot(), 'backend-configuration-files');
projectRoot.configurationFiles.map = () => path.join(projectRoot.configurationFiles(), 'maps');


projectRoot.frontend = () => path.join(projectRoot(), 'frontend-bikesurbanfleets');
projectRoot.frontend.src = () => path.join(projectRoot.frontend(), 'src');
projectRoot.frontend.main = () => path.join(projectRoot.frontend.src(), 'main');
projectRoot.frontend.renderer = () => path.join(projectRoot.frontend.src(), 'renderer');
projectRoot.frontend.assets = () => path.join(projectRoot.frontend(), 'assets');

projectRoot.schema = () => path.join(projectRoot(), 'schema');

projectRoot.tools = () => path.join(projectRoot(), 'tools');
projectRoot.tools.jsonSchemaValidator = () => path.join(projectRoot.tools(), 'jsonschema-validator/src');

projectRoot.build = () => path.join(projectRoot(), 'build');
projectRoot.build.schema = () => path.join(projectRoot.build(), 'schema');
projectRoot.build.frontend = () => path.join(projectRoot.build(), 'frontend');
projectRoot.build.jsonSchemaValidator = () => path.join(projectRoot.build(), 'jsonschema-validator');
projectRoot.build.dataAnalyser = () => path.join(projectRoot.build(), 'data-analyser');

projectRoot.fuseCache = () => path.join(projectRoot(), '.fusebox');
projectRoot.schemaCacheDefaults = () => path.join(projectRoot(), '.schema/defaults');
projectRoot.schemaLayoutCacheDefaults = () => path.join(projectRoot(), '.schema/schemas-and-form-definitions');

let production = false;
let schemaBuildPath = projectRoot.build.schema();

//BiciMad map
const overpassApiUrl = "http://overpass-api.de/api/interpreter?data=node(40.382824670624586,-3.636131286621094,40.46625392958603,-3.7508010864257817);out;";


Sparky.task('clean:backend', () => new Promise((resolve, reject) => {
    const maven = spawn('mvn', ['clean'], {
        cwd: projectRoot.backendRoot(),
        shell: true, // necessary for windows
        stdio: 'inherit' // pipe to calling process
    });

    log.time().green('start cleaning and installing backend maven dependencies').echo();

    maven.on('error', (error) => {
        log.red(error).echo();
    });

    maven.on('close', (code) => {
        if (code === 0) {
            log.time().green('finished cleaning and install of backend maven dependencies').echo();
            resolve();
        } else {
            log.time().red(`maven finished with error code ${code}`).echo();
            reject();
    }});
}));

Sparky.task('build:backend', () => new Promise((resolve, reject) => {
    const maven = spawn('mvn', ['clean', 'package'], {
        cwd: projectRoot.backendRoot(),
        shell: true, // necessary for windows
        stdio: 'inherit' // pipe to calling process
    });

log.time().green('Started backend-bikesurbanfleets building').echo();

maven.on('error', (error) => {
    log.red(error).echo();
});

maven.on('close', (code) => {
    if (code === 0) {
        let dirs = fs.readdirSync(projectRoot.backendRoot());
        dirs = dirs.filter(dirName => dirName.startsWith("backend-bikesurbanfleets"))
            .map(dirName => path.join(projectRoot.backendRoot(), `${dirName}/target`));

        dirs.forEach(dirName => {
                fs.readdirSync(dirName).filter((file) => file.endsWith('jar-with-dependencies.jar')).forEach((file) => {
                const target = path.join(dirName, file);
                const destination = path.join(projectRoot.build(), file.replace('-jar-with-dependencies', ''));

                fs.copySync(target, destination);

                log.time().green(`finished packaging ${file}`).echo();
            })
        });

            log.time().green('backend-bikesurbanfleets build finished').echo();
            resolve();
        } else {
            log.time().red(`maven finished with error code ${code}`).echo();
            reject();
        }
    });
}));

Sparky.task('build:schema', ['clean:cache:schema'], () => new Promise((resolve, reject) => {
    let command;
    let tsc;
    if(process.platform === 'darwin') {
        command = "'" + path.join(projectRoot(), 'node_modules/.bin/tsc') + "'";
        tsc = spawn(command, [], {
            cwd: projectRoot.schema(),
            shell: true,
            stdio: 'inherit'
        });
    } 	
    else {
        tsc = spawn(path.join(projectRoot(), 'node_modules/.bin/tsc'), [], {
            cwd: projectRoot.schema(),
            shell: true,
            stdio: 'inherit'
        });
    }

    tsc.on('error', (error) => {
        log.red(error).echo();
    });

    tsc.on('close', (code) => {
        if (code === 0) {
        log.time().green('compiling schemas').echo();

       // Schema processing 
        fs.readdirSync(projectRoot.schemaCacheDefaults()).filter((file) => file.endsWith('.js')).forEach((file) => {
            const allSchema = require(path.join(projectRoot.schemaCacheDefaults(), file));
            const schema = allSchema.default;
            const out = path.join(schemaBuildPath, `${file.slice(0, -3)}.json`);

            schema.errors.forEach((error) => {
                log.red(error).echo();
            });

            schema.write(out);

            log.time().green(`written schema to ${out}`).echo();
            
        });

        // Layout processing
        fs.readdirSync(projectRoot.schemaLayoutCacheDefaults()).filter((file) => file.endsWith('.js')).forEach((file) => {
            const allLayouts = require(path.join(projectRoot.schemaLayoutCacheDefaults(), file));
            const out = path.join(schemaBuildPath, `${file.slice(0, -3)}-layout.json`);

            if(allLayouts.layout){
                fs.writeJsonSync(out, allLayouts.layout, {spaces: 4});
                log.time().green(`writen layout to ${out}`).echo();
            }
        });

        //const globalLayout = require(path.join(projectRoot.schemaCacheDefaults(), 'global-config.js')).globalLayout;
        //fs.writeJSONSync();

        resolve();
        } else {
            log.time().red(`tsc finished with error code ${code}`).echo();
            reject();
        }
    });
}));

Sparky.task('build:schema-test', () => new Promise((resolve, reject) => {
    
    log.time().green('compiling schemas').echo();

    // Schema processing 
    fs.readdirSync(projectRoot.schemaCacheDefaults()).filter((file) => file.endsWith('.js')).forEach((file) => {
        const allSchema = require(path.join(projectRoot.schemaCacheDefaults(), file));
        const schema = allSchema.default;
        const out = path.join(schemaBuildPath, `${file.slice(0, -3)}.json`);

        schema.errors.forEach((error) => {
            log.red(error).echo();
        });

        schema.write(out);

        log.time().green(`written schema to ${out}`).echo();
        
    });

    // Layout processing
    fs.readdirSync(projectRoot.schemaLayoutCacheDefaults()).filter((file) => file.endsWith('.js')).forEach((file) => {
        const allLayouts = require(path.join(projectRoot.schemaLayoutCacheDefaults(), file));
        const out = path.join(schemaBuildPath, `${file.slice(0, -3)}-layout.json`);

        if(allLayouts.layout){
            fs.writeJsonSync(out, allLayouts.layout, {spaces: 4});
            log.time().green(`writen layout to ${out}`).echo();
        }
    });

    //const globalLayout = require(path.join(projectRoot.schemaCacheDefaults(), 'global-config.js')).globalLayout;
    //fs.writeJSONSync();

    resolve();
}));
Sparky.task('build:jsonschema-validator', () => {
    const fuse = FuseBox.init({
        homeDir: projectRoot.tools.jsonSchemaValidator(),
        output: path.join(projectRoot.build.jsonSchemaValidator(), '$name.js'),
        experimentalFeatures: true,
        plugins: [
            JSONPlugin()
        ]
    });

    fuse.bundle("jsonschema-validator.js").instructions(`>index.ts`);

    fuse.run();
});

Sparky.task('build:data-analyser', () => {
    const fuse = FuseBox.init({
        homeDir: projectRoot.frontend.src(),
        output: path.join(projectRoot.build.dataAnalyser(), '$name.js'),
        experimentalFeatures: true
    });

    const main = fuse.bundle('data-analyser.js').instructions('> [main/DataAnalyserTool.ts]');

    return fuse.run();
});
Sparky.task('build:frontend:main', () => {
    const fuse = FuseBox.init({
        homeDir: projectRoot.frontend.src(),
        output: path.join(projectRoot.build.frontend(), '$name.js'),
        target: 'server',
        experimentalFeatures: true,
        ignoreModules: ['electron'],
        plugins: [
            EnvPlugin({ target: production ? 'production' : 'development' }),
            JSONPlugin()
        ]
    });

    const main = fuse.bundle('main').instructions('>main/main.ts');

    
    if (!production) {
        // main.watch('main/**');
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
        homeDir: projectRoot.frontend.src(),
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
            }),
            JSONPlugin(),
            production && QuantumPlugin({
                bakeApiIntoBundle : false,
                target : 'electron',
                treeshake: true,
                removeExportsInterop: false,
                uglify: true
            })
        ]
    });

    const rendererEntrypoint = 'renderer/renderer.ts';

    const vendor = fuse.bundle('vendor').instructions(`~ ${rendererEntrypoint}`);
    const renderer = fuse.bundle('renderer').instructions(`!> [${rendererEntrypoint}]`);

    /*if (!production) {
        fuse.dev({ root: false }, (server) => {
            const app = server.httpServer.app;
            app.use('/renderer/', express.static(projectRoot.build.frontend()));
            app.get('*', (request, response) => {
                response.send(path.join(projectRoot.build.frontend(), 'index.html'));
            });
            // TODO: make the server close on electron window close (note: apparently not possible)
        });
        renderer.hmr().watch('renderer/**');
    }*/

    //Global css file to build
    const globalCss = path.join(projectRoot.frontend.renderer(), 'styles.css');
    const destination = path.join(projectRoot.build.frontend(), 'styles.css');
    fs.copySync(globalCss, destination);

    const packageProdOrig = path.join(projectRoot(), 'package_prod.json');
    const packageProdDest = path.join(projectRoot.build(), 'package.json');
    fs.copySync(packageProdOrig, packageProdDest);

    //Icons to build - Windows
    const originIconWin = path.join(projectRoot.frontend.assets(), 'icon.ico');
    const destinationIconWin = path.join(projectRoot.build(), 'icon.ico');
    fs.copySync(originIconWin, destinationIconWin);

    //Icons to build - Mac And Debian
    const originIconMac = path.join(projectRoot.frontend.assets(), 'icon.icns');
    const destinationIconMac = path.join(projectRoot.build(), 'icon.icns');
    fs.copySync(originIconMac, destinationIconMac);

    return fuse.run();
});

Sparky.task('gen-users:dev', () => new Promise((resolve, reject) => {
    const userGen = spawn('java', [
        `-jar`,
        `bikesurbanfleets-config-usersgenerator-1.0.jar`,
        '-entryPointsInput', '"' + path.join(projectRoot.configurationFiles(), 'entry-points-configuration.json') + '"',
        '-globalInput', '"' + path.join(projectRoot.configurationFiles(), 'global-configuration.json') + '"',
        '-output', '"' + projectRoot.configurationFiles() + '/users-configuration.json"',
        '-callFromFrontend'
    ], {
        cwd: projectRoot.build(),
        shell: true, // necessary for windows
        stdio: 'inherit' // pipe to calling process
    });

    log.time().green('Starting user generation').echo();

    userGen.on('error', (error) => {
        log.red(error).echo();
    });

    userGen.on('close', (code) => {
        if (code === 0) {
            log.time().green('Finished user generation').echo();
            resolve();
        } else {
            log.time().red(`User generation finished with code ${code}`).echo();
            reject();
    }});
}));

Sparky.task('simulate:dev', () => new Promise((resolve, reject) => {
    const userGen = spawn('java', [
        `-jar`,
        `bikesurbanfleets-core-1.0.jar`,
        '-globalConfig', '"' + path.join(projectRoot.configurationFiles(), 'global-configuration.json') + '"',
        '-usersConfig', '"' + path.join(projectRoot.configurationFiles(), 'users-configuration.json') + '"',
        '-stationsConfig', '"' + path.join(projectRoot.configurationFiles(), 'stations-configuration.json') + '"',
        '-historyOutput', '"' + path.join(projectRoot.build(), 'history') + '"',
        '-mapPath', '"' + path.join(projectRoot.configurationFiles.map(), 'madrid.osm') + '"',
        `-callFromFrontend`
    ], {
        cwd: projectRoot.build(),
        shell: true, // necessary for windows
        stdio: 'inherit' // pipe to calling process
    });

    log.time().green('Starting user generation').echo();

    userGen.on('error', (error) => {
        log.red(error).echo();
    });

    userGen.on('close', (code) => {
        if (code === 0) {
            log.time().green('Finished user generation').echo();
            resolve();
        } else {
            log.time().red(`User generation finished with code ${code}`).echo();
            reject();
    }});
}));

Sparky.task('copy:assets', async () => {
    await fs.copy(path.join(projectRoot.frontend(), 'assets'), path.join(projectRoot.build.frontend(), 'assets'));
});

Sparky.task('download-map:dev', () => new Promise((resolve, reject) => {
    log.time().green('Downloading osm map for development.').echo();
    if(!fs.existsSync(projectRoot.configurationFiles.map())) {
        fs.mkdirSync(projectRoot.configurationFiles.map());
    }
    let mapFile = path.resolve(projectRoot.configurationFiles.map(), 'madrid.osm');
    if(fs.existsSync(mapFile)) {
        log.time().green('Map is currently downloaded').echo();
        resolve();
    }
    else {
        let mapDownloadedFile = fs.createWriteStream(mapFile);
        this.request = request(overpassApiUrl);
        progress(this.request)
            .on('progress', (state) => {
                process.stdout.write('Speed: ' + parseFloat(state.speed / 1024 / 1024).toFixed(2) + 
                ' MB/s - Downloaded: ' + parseFloat(state.size.transferred / 1024 / 1024).toFixed(2) + ' MB\r');
            })
            .on('error', (err) => {
                console.log(err);
                this.request = undefined;
                reject();
            })
            .on('end', () => {
                this.request = undefined;
                log.time().green('Downloaded map in: ' + mapFile).echo();
                resolve();
            })
            .pipe(mapDownloadedFile);
    }
}));


Sparky.task('clean:build', () => Sparky.src(projectRoot.build()).clean(projectRoot.build()));

Sparky.task('clean:cache:fuse', () => Sparky.src(projectRoot.fuseCache()).clean(projectRoot.fuseCache()));

Sparky.task('clean:cache:schema', () => Sparky.src(projectRoot.schemaCacheDefaults()).clean(projectRoot.schemaCacheDefaults()));

Sparky.task('clean:cache', ['clean:cache:fuse', 'clean:cache:schema'], () => {});


Sparky.task('build:frontend', ['copy:assets', 'build:frontend:renderer', 'build:frontend:main'], () => {
    
});

Sparky.task('build:dev-backend', ['clean:build', 'clean:cache', 'build:backend', 'build:schema', 'build:jsonschema-validator', 'build:data-analyser'], () => {});


Sparky.task('configure:dev', ['download-map:dev', 'build:dev-backend'], () => {});

Sparky.task('build:dist', () => {
    production = true;
    Sparky.start('download-map:dev', 'build:dev-backend')
        .then(() => {
            return Sparky.start('build:frontend');
        })
        .catch((error) => {
            console.error(error);
        })
});

Sparky.task('build:schema:forBackend', () => {
    schemaBuildPath = path.join(projectRoot.backend(), 'schema');
    return Sparky.start('build:schema');
});