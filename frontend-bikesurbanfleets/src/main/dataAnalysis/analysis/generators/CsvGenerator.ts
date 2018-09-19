import { JsonObject } from "../../../../shared/util";
import { Station, User, Entity } from "../../systemDataTypes/Entities";
import { SystemGlobalInfo } from "../SystemGlobalInfo";
import { AbsoluteValue } from "../absoluteValues/AbsoluteValue";
import { SystemInfo } from "../absoluteValues/SystemInfo";
import { BikesBalanceQuality } from '../absoluteValues/bikesPerStation/BikesBalanceQuality';
import { RentalAndReturnAbsoluteValue } from "../absoluteValues/rentalsAndReturns/RentalAndReturnAbsoluteValue";
import { RentalAndReturnData } from "../absoluteValues/rentalsAndReturns/RentalAndReturnData";
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationAbsoluteValue } from "../absoluteValues/reservations/ReservationAbsoluteValue";
import { ReservationData } from "../absoluteValues/reservations/ReservationData";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import { EmptyStationInfo, EmptyStateData } from "../absoluteValues/bikesPerStation/EmptyStationInfo";
import * as json2csv from 'json2csv';
import * as fs from 'fs';

export class CsvGenerator {
	private readonly NUM_DATA: number = 4;   // number of absolute values of reservation and rental and return data  
    private csvPath: string;
    
    private entityInfoTitles: Array<string>;
    private globalInfoTitles: Array<string>;
    private emptyStationTitles: Array<string>;
    private bikesBalanceTitles: Array<string>;
    
   	private stationData: Array<JsonObject>;
   	private userData: Array<JsonObject>;
    private globalInfo: JsonObject;
    private emptyStationData: Array<JsonObject>;
    private bikesBalanceData: Array<JsonObject>; 
    
    public constructor(csvPath: string) {
        this.csvPath = csvPath;
        this.entityInfoTitles = new Array();
        this.globalInfoTitles = new Array();
        this.emptyStationTitles = new Array();
        this.stationData = new Array();
        this.userData = new Array();
        this.globalInfo = {};
        this.emptyStationData = new Array();
        this.bikesBalanceData = new Array();
  }
    
  public createJsonFor(entities: Array<Entity>, data: Array<JsonObject>, reservations: SystemInfo, rentalsAndReturns: SystemInfo): void {
    let i = 1;  // title index
    let j = 0;  // data index
      
    for (let entity of entities) {
        let jsonObj: JsonObject = {};
        
        jsonObj.id = entity.id;
        
        while (j < this.NUM_DATA) {   // adding reservations' data
            let reservationValues: AbsoluteValue | undefined = reservations.getData().absoluteValues.get(entity.id);
            if (reservationValues !== undefined) {
                let absValueList: Array<number> = reservationValues.getAbsoluteValuesAsArray();
                jsonObj[this.entityInfoTitles[i]] = absValueList[j]; 
                i++;
                j++;
            }
        }
        
        j = 0;
        while (j < this.NUM_DATA) {   // adding rentals and returns' data
            let rentalAndReturnValues: AbsoluteValue | undefined = rentalsAndReturns.getData().absoluteValues.get(entity.id);
            if (rentalAndReturnValues !== undefined) {
                let absValueList: Array<number> = rentalAndReturnValues.getAbsoluteValuesAsArray();
                jsonObj[this.entityInfoTitles[i]] = absValueList[j];
                i++;   
                j++;
            }
        }
        data.push(jsonObj);
		i = 1;
		j = 0;
    }
  }

    private async initEntityInfoTitles(): Promise<void> {
         this.entityInfoTitles.push('id');
         ReservationData.NAMES.forEach( (name) => this.entityInfoTitles.push(name));
         RentalAndReturnData.NAMES.forEach( (name) => this.entityInfoTitles.push(name));
         return;
    }
     
     private async initStationInfo(info: Map<string, SystemInfo>, stations: Array<Station>): Promise<void> {
         let reservations: SystemInfo | undefined = info.get(ReservationsPerStation.name);
         let rentalsAndReturns: SystemInfo | undefined = info.get(RentalsAndReturnsPerStation.name);
         if (reservations && rentalsAndReturns) {
           this.createJsonFor(stations, this.stationData, reservations, rentalsAndReturns);
         }
         return;
     }
    
    private async initUserInfo(info: Map<string, SystemInfo>, users: Array<User>): Promise<void> {
         let reservations: SystemInfo | undefined = info.get(ReservationsPerUser.name);
         let rentalsAndReturns: SystemInfo | undefined = info.get(RentalsAndReturnsPerUser.name);
         if (reservations && rentalsAndReturns) {   
           this.createJsonFor(users, this.userData, reservations, rentalsAndReturns);
         }
        return;
    }
    
    private async initGlobalInfo(globalInfo: SystemGlobalInfo): Promise<void> {
        SystemGlobalInfo.NAMES.forEach( (name) => this.globalInfoTitles.push(name));
        
        let data: Array<number> = globalInfo.getDataAsArray();
        for(let i = 0; i<data.length; i++) {
            this.globalInfo[this.globalInfoTitles[i]] = data[i];
        }
        return;
    }
    
    private async initEmptyStationInfo(info: Map<string, SystemInfo>): Promise<void> {
        let emptyStations: SystemInfo | undefined = info.get(EmptyStationInfo.name);
        if (emptyStations) {
             EmptyStateData.NAMES.forEach( (name) => {
             this.emptyStationTitles.push(name);
            });
            emptyStations.getData().absoluteValues.forEach( (v, k) => {
                let jsonObj: JsonObject = {};
                jsonObj[this.emptyStationTitles[0]] = v.intervalsToString();
                jsonObj[this.emptyStationTitles[1]] = v.totalTime;
                this.emptyStationData.push(jsonObj);
            });
        }
        return;
    }
    
    private async initBikesBalanceInfo(info: Map<string, SystemInfo>): Promise<void> {
        this.bikesBalanceTitles[0] = 'id';
        this.bikesBalanceTitles[1] = 'balance quality';
        let bikesBalance: SystemInfo | undefined = info.get(BikesBalanceQuality.name); 
        if (bikesBalance) {
            bikesBalance.getData().absoluteValues.forEach( (quality, stationId) => {
                let obj: JsonObject = {};
                obj[this.bikesBalanceTitles[0]] = stationId;
                obj[this.bikesBalanceTitles[1]] = quality;
                this.bikesBalanceData.push(obj);
            });
        }
        return;
    }

	   public async generate(entityInfo: Map<string, SystemInfo>, globalInfo: SystemGlobalInfo, stations: Array<Station>, users: Array<User>): Promise<void> {
         await this.initEntityInfoTitles();
           
         this.initStationInfo(entityInfo, stations).then( () => {
             this.transformStationJsonToCsv();
         });
           
         this.initUserInfo(entityInfo, users).then( () => {
             this.transformUserJsonToCsv();
         });
           
         this.initGlobalInfo(globalInfo).then( () => { 
             this.transformGlobalInfoJsonToCsv();
         });
           
           this.initEmptyStationInfo(entityInfo).then( () => {
               this.transformEmptyStationJsonToCsv();
           });
           
           this.initBikesBalanceInfo(entityInfo).then( () => {
               this.transformBikesBalanceJsonToCsv();
           });
         return;
   	}

	private transformStationJsonToCsv(): void {
        let csv = json2csv({ data: this.stationData, fields: this.entityInfoTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/stations.csv`, csv, (err) => {
          if (err) throw err;
          console.log('stations file saved');
        });
	}
    
    private transformUserJsonToCsv(): void {
        let csv = json2csv({ data: this.userData, fields: this.entityInfoTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/users.csv`, csv, (err) => {
          if (err) throw err;
          console.log('user file saved');
        });
    }

    private transformGlobalInfoJsonToCsv(): void {
        let csv = json2csv({data: [this.globalInfo], fields: this.globalInfoTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile(`${this.csvPath}/global_values.csv`, csv, (err) => {
            if (err) throw err;
            console.log('global values file saved');
        });
    }
    
    private transformEmptyStationJsonToCsv(): void {
        let csv = json2csv({data: this.emptyStationData, fields: this.emptyStationTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile(`${this.csvPath}/empty_stations.csv`, csv, (err) => {
            if (err) throw err;
            console.log('empty stations file saved');
        });        
    }

	private checkFolders(): void {
        if(!fs.existsSync(this.csvPath)) {
            fs.mkdirSync(this.csvPath);
        }
    }
    
    private transformBikesBalanceJsonToCsv(): void {
        let csv = json2csv({ data: this.bikesBalanceData, fields: this.bikesBalanceTitles, withBOM: true, del: ';' });
        this.checkFolders();
        fs.writeFile (`${this.csvPath}/bikesBalance.csv`, csv, (err) => {
          if (err) throw err;
          console.log('Bikes balance quality file saved');
        });
    }
    
}  