import { isPlainObject } from 'lodash';
import * as Ajv from 'ajv';

export interface PlainObject {
    [key: string]: any;
    [key: number]: any;
}

export interface JsonObject {
    [key: string]: JsonValue;
}

export interface JsonArray extends Array<JsonValue> {}

export interface ValidationInfo {
    result: boolean;
    errors: string;
}

export type JsonValue = null | string | number | boolean | JsonArray | JsonObject;

export namespace Tree {

    interface TreeNode {
        parent: PlainObject;
        name: string;
    }

    function fetch(tree: PlainObject, path: string): TreeNode {
        const names = path.split('.');
        let parent = tree;

        names.slice(0, -1).forEach((key) => parent = parent[key]);

        return {
            parent: parent,
            name: names.slice(-1)[0],
        };
    }

    function traverseInternal(tree: PlainObject, atLeaf: LeafCallback, path: Array<string>): PlainObject {
        Object.keys(tree).forEach((key) => {
            if (isPlainObject(tree[key])) {
                traverseInternal(tree[key], atLeaf, [...path, key]);
            } else {
                atLeaf(tree, key, [...path, key]);
            }
        });
        return tree;
    }

    export type LeafCallback = (parent: PlainObject, key: string, path: Array<string>) => void;

    export function get(tree: PlainObject, path: string): any {
        const node = fetch(tree, path);
        return node.parent[node.name];
    }

    export function set(tree: PlainObject, path: string, value: any) {
        const node = fetch(tree, path);
        node.parent[node.name] = value;
    }

    export function traverse(tree: PlainObject, atLeaf: LeafCallback): PlainObject {
        return traverseInternal(tree, atLeaf, []);
    }
}

export namespace Geo {
    export const RADIUS = 6371e3;

    export interface Point {
        latitude: number;
        longitude: number;
    }

    export interface EncodedRoute {
        totalDistance: number;
        encodedPoints: string;
    }

    export interface Route {
        totalDistance: number;
        points: Array<Point>;
    }

    export function toRadians(value: number) {
        return value * toRadians.factor;
    }

    export function decodeRoute(encroute: EncodedRoute) {
        if( encroute) {
            return { totalDistance: encroute.totalDistance,
            points: decode(encroute.encodedPoints };
        } else {
        return null;
        }
    }
            
    export function decode(encoded: string) {
        // array that holds the points

        var points=[ ]
        var index = 0, len = encoded.length;
        var lat = 0, lng = 0;
        while (index < len) {
        var b, shift = 0, result = 0;
        do {

            b = encoded.charAt(index++).charCodeAt(0) - 63;//finds ascii //and substract it by 63
              result |= (b & 0x1f) << shift;
              shift += 5;
             } while (b >= 0x20);


       var dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
       lat += dlat;
      shift = 0;
      result = 0;
        do {
        b = encoded.charAt(index++).charCodeAt(0) - 63;
        result |= (b & 0x1f) << shift;
       shift += 5;
         } while (b >= 0x20);
     var dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
     lng += dlng;
 
    points.push({latitude:( lat / 1E5),longitude:( lng / 1E5)})  
 
        }
        return points
    }

    export namespace toRadians {
        export const factor = Math.PI / 180;
    }

    export function haversine(value: number) {
        return Math.sin(value / 2) ** 2;
    }

    export function distance(p1: Point, p2: Point) {
        const f = [p1.latitude, p2.latitude].map(toRadians);
        const l = [p1.longitude, p2.longitude].map(toRadians);
        const h = haversine(f[1] - f[0]) + Math.cos(f[0]) * Math.cos(f[1]) * haversine(l[1] - l[0]);
        return 2 * RADIUS * Math.asin(Math.sqrt(h));
    }

    export function distances(route: Route) {
        const d: Array<number> = [];

        for (let i = 1; i < route.points.length; i++) {
            d.push(distance(route.points[i - 1], route.points[i]));
        }

        return d;
    }
}

export function safe(object: PlainObject, path: string) {
    return path.split('.').reduce((r, v) => r && r[v], object);
}

export function validate(schemaFile: string, jsonFile: string): ValidationInfo {
    let ajv = new Ajv({$data: true});
    let valid = ajv.validate(schemaFile, jsonFile);
    if(valid) {
        return {result: true, errors: ajv.errorsText()};
    }
    return {result: false, errors: ajv.errorsText()};
}