import { HistoryEntitiesJson } from "../../../shared/history";
import { HistoryReader } from "../../util";
import { User } from "../systemDataTypes/Entities";
import { RentalsAndReturnsPerStation } from "./absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { ReservationsPerStation } from "./absoluteValues/reservations/ReservationsPerStation";

export class SystemGlobalInfo {
  private numberUsers: number;
  private data: Map<string, any>;
  
  private totalRentals: number;
  private totalReturns: number; 
  private totalBikeReservations: number;
  private totalSlotReservations: number;
  
  private successfulRentals: number;
  private successfulReturns: number;
  private successfulBikeReservations: number;
  private successfulSlotReservations: number;  
  
  private failedRentals: number;
  private failedReturns: number;
  private failedBikeReservations: number;
  private failedSlotReservations: number;
  
  private demandSatisfaction: number;
  private rentalEfficiency: number;
  private returnEfficiency: number;
  
  public constructor(data: any) {
    this.data = data;
    this.totalRentals = 0;
    this.totalReturns = 0; 
    this.totalBikeReservations = 0;
    this.totalSlotReservations = 0;
  
    this.successfulRentals = 0;
    this.successfulReturns = 0;
    this.successfulBikeReservations = 0;
    this.successfulSlotReservations = 0;  
  
    this.failedRentals = 0;
    this.failedReturns = 0;
    this.failedBikeReservations = 0;
    this.failedSlotReservations = 0;
  
    this.demandSatisfaction = 0;
  }
  
  public async init(path: string, schemaPath?: string | null): Promise<void> {
    let history: HistoryReader = await HistoryReader.create(path, schemaPath);
    let entities: HistoryEntitiesJson = await history.getEntities("users");
    let users: Array<User> = entities.instances;
    this.numberUsers = users.length;
    return;
  }
  
  
  public calculateGlobalData(): void {
    let reservations: ReservationsPerStation = this.data.get(ReservationsPerStation.name);
    reservations.getData().forEach( (v, k) => this.successfulBikeReservations += v.absoluteValues.successfulBikeReservations); 
    reservations.getData().forEach( (v, k) => this.successfulSlotReservations += v.absoluteValues.successfulSlotReservations);
    reservations.getData().forEach( (v, k) => this.failedBikeReservations += v.absoluteValues.failedBikeReservations);
    reservations.getData().forEach( (v, k) => this.failedSlotReservations += v.absoluteValues.failedSlotReservations);
    
    this.totalBikeReservations = this.successfulBikeReservations + this.failedBikeReservations;
    this.totalSlotReservations = this.successfulSlotReservations + this.failedSlotReservations;
      
    let rentalsAndReturns: RentalsAndReturnsPerStation = this.data.get(RentalsAndReturnsPerStation.name);
    rentalsAndReturns.getData().forEach( (v, k) => this.successfulRentals += v.absoluteValues.successfulRentals);
    rentalsAndReturns.getData().forEach( (v, k) => this.successfulReturns += v.absoluteValues.successfulReturns);
    rentalsAndReturns.getData().forEach( (v, k) => this.failedRentals += v.absoluteValues.failedRentals);
    rentalsAndReturns.getData().forEach( (v, k) => this.failedReturns += v.absoluteValues.failedReturns);
    
    this.totalRentals = this.successfulRentals + this.failedRentals;
    this.totalReturns = this.successfulReturns + this.failedReturns;
    
    this.demandSatisfaction = this.successfulRentals / this.numberUsers;  // DS = SH / N
    this.rentalEfficiency = this.successfulRentals / (this.numberUsers + this.failedRentals);  // HE = SH / (N+FH)
    this.returnEfficiency = this.successfulReturns / (this.successfulRentals + this.failedReturns); // RE = SR / (SH+FR)
  }

  public getDemandSatisfaction(): number {
    return this.demandSatisfaction;
  }

  public getRentalEfficiency(): number {
    return this.rentalEfficiency;
  }

  public getReturnEfficiency(): number {
    return this.returnEfficiency;
  }
   
}