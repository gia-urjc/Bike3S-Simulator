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
