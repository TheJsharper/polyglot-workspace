import nx from "@nx/eslint-plugin";

export default [
    ...nx.configs["flat/base"],
    ...nx.configs["flat/typescript"],
    ...nx.configs["flat/javascript"],
    {
      "ignores": [
        "**/dist",
        "**/out-tsc",
        "**/vitest.config.*.timestamp*"
      ]
    },
    {
        files: [
            "**/*.ts",
            "**/*.tsx",
            "**/*.js",
            "**/*.jsx"
        ],
        rules: {
            "@nx/enforce-module-boundaries": [
                "error",
                {
                    enforceBuildableLibDependency: true,
                    allow: [
                        "^.*/eslint(\\.base)?\\.config\\.[cm]?[jt]s$"
                    ],
                    depConstraints: [
                        {
                            sourceTag: "type:app",
                            onlyDependOnLibsWithTags: ["type:shell", "type:dashboard", "type:shared", "type:service"]
                        },
                        {
                            sourceTag: "type:remote",
                            onlyDependOnLibsWithTags: ["type:view", "type:dashboard", "type:widget", "type:shared", "type:service"]
                        },
                        {
                            sourceTag: "type:shell",
                            onlyDependOnLibsWithTags: ["type:remote", "type:layout", "type:view", "type:dashboard", "type:widget", "type:shared", "type:service"]
                        },
                        {
                            sourceTag: "type:layout",
                            onlyDependOnLibsWithTags: ["type:widget", "type:shared", "type:service"]
                        },
                        {
                            sourceTag: "type:view",
                            onlyDependOnLibsWithTags: ["type:widget", "type:shared", "type:service"]
                        },
                        {
                            sourceTag: "type:dashboard",
                            onlyDependOnLibsWithTags: ["type:widget", "type:shared", "type:service"]
                        },
                        {
                            sourceTag: "type:widget",
                            onlyDependOnLibsWithTags: ["type:shared", "type:service"]
                        },
                        {
                            sourceTag: "type:shared",
                            onlyDependOnLibsWithTags: ["type:shared", "type:service"]
                        },
                        {
                            sourceTag: "type:service",
                            onlyDependOnLibsWithTags: ["type:service"]
                        }
                    ]
                }
            ]
        }
    },
    {
        files: [
            "**/*.ts",
            "**/*.tsx",
            "**/*.cts",
            "**/*.mts",
            "**/*.js",
            "**/*.jsx",
            "**/*.cjs",
            "**/*.mjs"
        ],
        // Override or add rules here
        rules: {}
    }
];
