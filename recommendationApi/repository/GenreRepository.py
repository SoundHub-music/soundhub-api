from typing import Self

import pandas as pd

from database import Database
from utils import logger


class GenreRepository:
    instance: Self | None = None

    def __init__(self, db: Database):
        self.__db = db

    def __new__(cls, *args, **kwargs):
        if cls.instance is None:
            cls.instance = super().__new__(cls)
        return cls.instance


    def get_favorite_genres(self) -> pd.DataFrame:
        try:
            with self.__db as engine:
                query = "SELECT * FROM user_favorite_genres;"
                return pd.read_sql_query(query, con=engine)
        except Exception as e:
            logger.error(f"get_favorite_genres[1]: {e}")