from functools import lru_cache
from typing import Self

import pandas as pd

from database import Database

class GenreRepository:
    instance: Self | None = None

    def __init__(self, db: Database):
        self.__db = db

    def __new__(cls, *args, **kwargs):
        if cls.instance is None:
            cls.instance = super().__new__(cls)
        return cls.instance

    @lru_cache(maxsize=1)
    def get_favorite_genres_grouped_by_users(self):
        with self.__db as engine:
            query = "SELECT user_id, ARRAY_AGG(genre_id) as genre_id FROM user_favorite_genres GROUP BY user_id;"

            return pd.read_sql_query(query, con=engine)