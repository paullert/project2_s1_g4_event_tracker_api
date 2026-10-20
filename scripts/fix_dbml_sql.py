#!/usr/bin/env python3

import re
import sys
from pathlib import Path


def fix_sql(sql: str) -> str:
    # Fix known malformed DBML-generated defaults.
    sql = re.sub(
        r"DEFAULT\s+\(\s*\*?now\*?\s*\(\s*\)\s*\)",
        "DEFAULT now()",
        sql,
        flags=re.IGNORECASE,
    )

    sql = re.sub(
        r"DEFAULT\s+\(\s*\*?USER\*?\s*\)",
        "DEFAULT 'USER'::user_role",
        sql,
        flags=re.IGNORECASE,
    )

    # Ensure enum default is explicitly typed.
    sql = re.sub(
        r'("role"\s+user_role\s+NOT NULL\s+)DEFAULT\s+'
        r"'USER'(?!\s*::)",
        r"\1DEFAULT 'USER'::user_role",
        sql,
        flags=re.IGNORECASE,
    )

    # Reject missing values in identity columns.
    # Do not guess IDs: foreign keys may depend on them.
    if re.search(
        r'INSERT\s+INTO\s+"users"\s*\(\s*"user_id"',
        sql,
        flags=re.IGNORECASE,
    ):
        # Validation of actual IDs is left to PostgreSQL.
        pass

    # Keep identity sequences synchronized after seeding.
    sequences = [
        ("users", "user_id"),
        ("locations", "location_id"),
        ("events", "event_id"),
    ]

    statements = []

    for table, column in sequences:
        statements.append(f"""
SELECT setval(
    pg_get_serial_sequence('{table}', '{column}'),
    COALESCE((SELECT MAX("{column}") FROM "{table}"), 1),
    EXISTS(SELECT 1 FROM "{table}")
);
""")
    # Fix DBML Converter issue, turns "unique" into "NOT NULL UNIQUE"
    sql = re.sub(
        r'("google_subject"\s+varchar\(255\)\s+UNIQUE)\s+NOT NULL',
        r'\1',
        sql,
        flags=re.IGNORECASE,
    )

    # Append after all inserts and transaction blocks.
    sql = sql.rstrip() + "\n\n"
    sql += "-- Synchronize identity sequences\n"
    sql += "\n".join(statements)

    return sql


def main():
    if len(sys.argv) != 2:
        raise SystemExit(
            "Usage: fix_dbml_sql.py <sql-file>"
        )

    path = Path(sys.argv[1])
    sql = path.read_text(encoding="utf-8")

    corrected = fix_sql(sql)

    path.write_text(corrected, encoding="utf-8")

    print(f"Processed PostgreSQL SQL: {path}")


if __name__ == "__main__":
    main()
