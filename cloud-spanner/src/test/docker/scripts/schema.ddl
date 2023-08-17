create table FeatureToggle
(
    featureName          string(100),
    enabled              bool not null,
    strategyId           string(100),
    strategyParamsNames  array<string(1000)>,
    strategyParamsValues array<string(1000)>
) primary key ( featureName );
