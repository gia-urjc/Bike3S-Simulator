process.env.SPARKY_LOG = false;

const { FuseBox, Sparky } = require('fuse-box');
const { EnvPlugin, CSSPlugin, CSSResourcePlugin, RawPlugin, WebIndexPlugin } = require('fuse-box');

const log = require('fliplog');
const express = require('express');

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs-extra');

const projectRoot = () => process.cwd();


/* ==============================
 *
 * PROJECT RELEASE INFORMATION - BACKEND
 *
 * ============================== */

const groupId = 'es.urjc.ia';
const projectName = 'bikesurbanfleets';
const version = '1.0-SNAPSHOT';

/* ============================== */

projectRoot.mavenLocalRepository = () => path.join(projectRoot(), 'libs/maven-local-repo');

projectRoot.frontend = () => path.join(projectRoot(), 'frontend-bikesurbanfleets');
projectRoot.frontend.src = () => path.join(projectRoot.frontend(), 'src');
projectRoot.frontend.main = () => path.join(projectRoot.frontend.src(), 'main');
projectRoot.frontend.renderer = () => path.join(projectRoot.frontend.src(), 'renderer');

projectRoot.schema = () => path.join(projectRoot(), 'schema');

projectRoot.jsonschemaValidator = () => path.join(projectRoot(), 'jsonschema-validator/src');

projectRoot.build = () => path.join(projectRoot(), 'build');
projectRoot.build.schema = () => path.join(projectRoot.build(), 'schema');
projectRoot.build.frontend = () => path.join(projectRoot.build(), 'frontend');
projectRoot.build.jsonschemaValidator = () => path.join(projectRoot.build(), 'jsonschema-validator');

projectRoot.fuseCache = () => path.join(projectRoot(), '.fusebox');
projectRoot.schemaCache = () => path.join(projectRoot(), '.schema');

/* ==============================
 *
 * BACKEND COMPILATION FUNCTION
 *
 * ============================== */

const backendBuildModule = (moduleName) => {
    return new Promise((resolve, reject) => {
        const backendModulePath = () => path.join(projectRoot(), `backend/backend-${projectName}-${moduleName}`);
        const backendModuleMavenTarget = () => path.join(backendModulePath(), 'target');
        const maven = spawn('mvn', ['clean', 'package'], {
            cwd: backendModulePath(),
            shell: true, // necessary for windows
            stdio: 'inherit' // pipe to calling process
        });

        log.time().green(`start packaging backend-${moduleName} jar`).echo();

        maven.on('error', (error) => {
            log.red(error).echo();
        });

        maven.on('close', (code) => {
            if (code === 0) {
                const jar = fs.readdirSync(backendModuleMavenTarget()).find((file) => file.endsWith('.jar'));
                const target = path.join(backendModuleMavenTarget(), jar);
                const destination = path.join(projectRoot.build(), `backend-${moduleName}.jar`);

                fs.copySync(target, destination);

                log.time().green(`finished packaging backend-${moduleName} jar`).echo();
                Sparky.start(`install:maven-local-dependency:${moduleName}`);

                resolve();
            } else {
                log.time().red(`maven finished with error code ${code}`).echo();
                reject();
            }
        });
    });
};

const cleanMavenLocalDependency = (moduleName) => {
    return new Promise((resolve, reject) => {
        const backendModulePath = () => path.join(projectRoot(), `backend/backend-${projectName}-${moduleName}`);

        const maven = spawn('mvn', ['dependency:purge-local-repository', `-DmanualInclude=${groupId}:${projectName}-${moduleName}`], {
            cwd: backendModulePath(),
            shell: true, // necessary for windows
            stdio: 'inherit' // pipe to calling process
        });

        log.time().green(`cleaning maven local repository for backend-${moduleName}`).echo();

        maven.on('error', (error) => {
            log.red(error).echo();
        });

        maven.on('close', (code) => {
            if (code === 0) {
                log.time().green(`finished maven local repository clean for backend-${moduleName}`).echo();
                resolve();
            } else {
                log.time().red(`maven finished with error code ${code}`).echo();
                reject();
            }
        });
    });
};

const installMavenLocalDependency = (moduleName) => {
    return new Promise((resolve, reject) => {
        const maven = spawn('mvn',
            ['org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file',
                '-Dfile=' + projectRoot.build() + `/backend-${moduleName}.jar`,
                '-DgroupId=' + groupId,
                `-DartifactId=${projectName}-${moduleName}`,
                '-Dversion=' + version,
                '-Dpackaging=jar',
                '-DlocalRepositoryPath='+ projectRoot.mavenLocalRepository()], {
                cwd: projectRoot(),
                shell: true, // necessary for windows
                stdio: 'inherit' // pipe to calling process
            });

        log.time().green(`Installing dependency ${projectName}-${moduleName} in maven local repository`).echo();

        maven.on('error', (error) => {
            log.red(error).echo();
        });

        maven.on('close', (code) => {
            if (code === 0) {
                log.time().green(`finished installation of ${projectName}-${moduleName} in maven local repository`).echo();
                resolve();
            } else {
                log.time().red(`maven finished with error code ${code}`).echo();
                reject();
            }
        });
    });
}

/* ============================== */

/* ==============================
 *
 * TASKS
 *
 * ============================== */

let production = false;
let schemaBuildPath = projectRoot.build.schema();

/* ====
   COMMON MODULE
   ==== */
Sparky.task('build:backend-common', [
    'clean:maven-local-dependency:common',
    'build:backend-common-jar'], () => {});

Sparky.task('build:backend-common-jar', () => new Promise((resolve, reject) => {
    backendBuildModule('common');
}));

Sparky.task('install:maven-local-dependency:common', () => new Promise((resolve, reject) => {
    installMavenLocalDependency('common');
}));

Sparky.task('clean:maven-local-dependency:common', () => new Promise((resolve, reject) =>  {
    resolve(cleanMavenLocalDependency('common'));
}));

Sparky.task('build:backend-core', () => new Promise((resolve, reject) => {
    backendBuildModule('core');
}));

/* ====
   USERS GENERATOR MODULE
   ==== */

Sparky.task('build:backend-usersgenerator', [
    'build:backend-usersgenerator-jar'], () => {});

Sparky.task('build:backend-usersgenerator-jar', ['clean:maven-local-dependency:usersgenerator'], () => new Promise((resolve, reject) => {
    backendBuildModule('usersgenerator')
}));

Sparky.task('install:maven-local-dependency:usersgenerator', () => new Promise((resolve, reject) => {
    installMavenLocalDependency('usersgenerator');
}));

Sparky.task('clean:maven-local-dependency:usersgenerator', () => new Promise((resolve, reject) =>  {
    resolve(cleanMavenLocalDependency('usersgenerator'));
}));

/*
 =====
 */


Sparky.task('build:backend',
    ['build:backend-common', 'clean:maven-local-dependency:common', 'install:maven-local-dependency:common',
    'build:backend-core'], () => {});

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
        homeDir: projectRoot.frontend.src(),
        output: path.join(projectRoot.build.frontend(), '$name.js'),
        target: 'server',
        experimentalFeatures: true,
        cache: !production,
        plugins: [
            EnvPlugin({ target: production ? 'production' : 'development' }),
        ]
    });

    const main = fuse.bundle('main').instructions('> [main/main.ts]');

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
            })
        ]
    });

    const vendor = fuse.bundle('vendor').instructions('~ renderer/renderer.ts');
    const renderer = fuse.bundle('renderer').instructions('!> [renderer/renderer.ts]');

    /*if (!production) {
        fuse.dev({ root: false }, (server) => {
            const app = server.httpServer.app;
            app.use('/renderer/', express.static(projectRoot.build.frontend()));
            app.get('*', (request, response) => {
                response.send(path.join(projectRoot.build.frontend(), 'index.html'));
            });
            // TODO: make the server close on electron window close (note: apparently not possible)
        });

        renderer.watch().hmr();
    }*/

    return fuse.run();
});

Sparky.task('copy:assets', async () => {
    await fs.copy(path.join(projectRoot.frontend(), 'assets'), path.join(projectRoot.build.frontend(), 'assets'));
});

Sparky.task('clean:build', () => Sparky.src(projectRoot.build()).clean(projectRoot.build()));

Sparky.task('clean:cache:fuse', () => Sparky.src(projectRoot.fuseCache()).clean(projectRoot.fuseCache()));
Sparky.task('clean:cache:schema', () => Sparky.src(projectRoot.schemaCache()).clean(projectRoot.schemaCache()));

Sparky.task('clean:cache', ['clean:cache:fuse', 'clean:cache:schema'], () => {});

Sparky.task('build:frontend', ['copy:assets', 'build:frontend:renderer', 'build:frontend:main'], () => {
    return Sparky.src(path.join(projectRoot(), 'package.json')).dest(projectRoot.build());
});

Sparky.task('build:dev', ['clean:build', 'clean:cache', 'build:backend-core', 'build:schema', 'build:frontend', 'build:jsonschema-validator'], () => {});

Sparky.task('build:dist', () => {
    production = true;
    return Sparky.start('build:dev');
});

Sparky.task('build:schema:forBackend', () => {
    schemaBuildPath = path.join(projectRoot.backend(), 'schema');
    return Sparky.start('build:schema');
});