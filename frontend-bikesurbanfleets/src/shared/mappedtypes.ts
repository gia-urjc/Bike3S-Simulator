export type Diff<T extends string, U extends string> = (
    { [P in T]: P } & { [P in U]: never } & { [x: string]: never }
)[T];

export type FlatKeys<T, K extends keyof T = keyof T> = {
    [P in K]: keyof T[P]
}[K];

export type Flatten<T, K extends keyof T = keyof T> = {
    [S in FlatKeys<T, K>]: {
        [P in keyof T]: T[P] & Record<FlatKeys<T, Diff<keyof T, P>>, never>
    }[K][S]
};

export type False = '0';
export type True = '1';

export type If<C extends True | False, Then, Else> = { '0': Else, '1': Then }[C];

export type PureKeys<T> = Diff<keyof T, keyof Object>;

export type Is<T, U> = (Record<PureKeys<T & U>, False> & Record<any, True>)[Diff<PureKeys<T>, PureKeys<U>>];

export type UnArray<T extends Array<any>> = T[0];

export type Safe<T> = T & Object;
