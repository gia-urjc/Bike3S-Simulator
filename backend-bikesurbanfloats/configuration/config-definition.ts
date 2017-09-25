interface ConfigJson {
    stations: Array<{
        bikes: number | Array<{
            id: number
        }>,
        capacity: number,
        position: {
            latitude: number,
            longitude: number
        }
    }>,
    entryPoints: Array<{
        distribution: "poisson" | "random",
        distributionParameter: number,
        position: {
            latitude: number,
            longitude: number
        }
    }>
}