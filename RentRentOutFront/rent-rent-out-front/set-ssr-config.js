#!/usr/bin/env node
// Skripta koja garantuje da angular.json ima SSR opcije.
// Pokretati pre svakog builda ako postoji problem sa revertovanjem konfiguracije.
const fs = require('fs');
const path = require('path');

const angularJsonPath = path.join(__dirname, 'angular.json');
const config = JSON.parse(fs.readFileSync(angularJsonPath, 'utf8'));

const opts = config.projects['rent-rent-out-front'].architect.build.options;
opts.server = 'src/main.server.ts';
opts.ssr = { entry: 'server.ts' };
opts.prerender = false;

fs.writeFileSync(angularJsonPath, JSON.stringify(config, null, 2));
console.log('SSR config set in angular.json');
