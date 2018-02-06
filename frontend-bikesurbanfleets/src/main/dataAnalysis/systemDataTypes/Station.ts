export class Station { 
  private availableBikes: number;
  
  public constructor(bikes: number) {
    this.availableBikes = bikes;
  }
  
  public getAvailableBikes(): number {
    return this.availableBikes;
  }
  
  public addBike(): void {
    this.availableBikes++;
  }
  
  public substractBike(): void {
    this.availableBikes--;
  }
}