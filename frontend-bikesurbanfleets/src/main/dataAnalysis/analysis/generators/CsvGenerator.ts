import { HistoryEntitiesJson } from "../../../../shared/history";
import { JsonObject } from "../../../../shared/util";
import { HistoryReader } from "../../../util";
import { Station, User, Entity } from "../../systemDataTypes/Entities";
import { Info } from "../absoluteValues/Info";
import { RentalsAndReturnsInfo } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsInfo";
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsInfo } from "../absoluteValues/reservations/ReservationsInfo";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import * as json2csv from 'json2csv';
import * as fs from 'fs';

export class CsvGenerator {
  private titles: Array<string>;
  private stationData: Array<JsonObject>;
  private userData: Array<JsonObject>;
  private path: string;
  
  public constructor(path: string) {
    this.titles = new Array();
    this.stationData = new Array();
    this.userData = new Array();
    this.path = path;
  }
    
	 public async generate(data: Map<string, any>): Promise<void> {
      try {
          await this.init(data);
          this.transformToCsv();
      }
      catch(error) {
          throw new Error('Error initializing csv generator: '+error);
      }
      return;
	 }
     
  public createJsonFor(entities: Array<Entity>, data: Array<JsonObject>, reservations: Info, rentalsAndReturns: Info): void {
    let i: number = 1;  // title index
    let j: number = 0;  // data index
      
    for (let entity of entities) {
        let jsonObj: JsonObject = {};
        
        jsonObj.id = entity.id;
        
        while (j < 4) {   // adding reservations' data
            jsonObj[this.titles[i]] = reservations.getReservations().getData()[j];
            i++;
            j++;
        }
        
        j = 0;
        while (j < 4) {   // adding rentals and returns' data
            jsonObj[this.titles[i]] = rentalsAndReturns.getRentalsAndReturns().getData()[j];
            i++;   
            j++;
        }
        data.push(jsonObj);
    }
  } 

	 public async init(data: Map<string, Info>): Promise<void> {
      this.titles.push('id');
      ReservationsInfo.getNames().forEach( (name) => this.titles.push(name));
      RentalsAndReturnsInfo.getNames().forEach( (name) => this.titles.push(name));
          
      let history: HistoryReader = await HistoryReader.create(this.path);
         
      let historyStations: HistoryEntitiesJson = await history.getEntities('stations');    
      let stations: Array<Station> = <Station[]> historyStations.instances;
         
      let reservations: Info | undefined = data.get(ReservationsPerStation.name);
      let rentalsAndReturns: Info | undefined = data.get(RentalsAndReturnsPerStation.name);
      if (reservations && rentalsAndReturns) {
        this.createJsonFor(stations, this.stationData, reservations, rentalsAndReturns);
      }
         
      let historyUsers: HistoryEntitiesJson = await history.getEntities('users');
      let users: Array<User> = <User[]> historyUsers.instances;
             
      reservations = data.get(ReservationsPerUser.name);
      rentalsAndReturns = data.get(RentalsAndReturnsPerUser.name);
      if (reservations && rentalsAndReturns) {   
        this.createJsonFor(users, this.userData, reservations, rentalsAndReturns);
      }
	}

	public transformToCsv(): void {
    let csv = json2csv({ data: this.stationData, fields: this.titles, withBOM: true });
    fs.writeFile ('stations.csv', csv, (err) => {
      if (err) throw err;
      console.log('stations file saved');
    });
    csv = json2csv({ data: this.userData, fields: this.titles, withBOM: true });
    fs.writeFile ('users.csv', csv, (err) => {
      if (err) throw err;
      console.log('user file saved');
    });    
	}

}  