declare interface ConicGradientOptions {
    stops: string,
    size?: number,
    repeating?: boolean,
}

declare class ConicGradient {

    /*static ColorStop: {
        new(): this.ColorStop
    };*/

    // stops: Array<ColorStop>;
    size: number;
    repeating: boolean;

    from: number;

    canvas: HTMLCanvasElement;
    context: CanvasRenderingContext2D;

    png: string;
    svg: string;
    dataURL: string;
    blobURL: string;
    r: number;

    constructor(options: ConicGradientOptions);

    paint(): void;
}
