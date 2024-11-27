from typing import Self

from dotenv import dotenv_values
from sqlalchemy import Engine, create_engine

from utils import logger


class Database:
    instance: Self | None = None

    def __init__(self):
        self.__config: dict[str, str | None] = dotenv_values()

    def __new__(cls, *args, **kwargs):
        if cls.instance is None:
            cls.instance = super().__new__(cls)
        return cls.instance

    def __enter__(self) -> Engine:
        self.engine: Engine = self.get_db_connection()
        logger.info('Connected to database')
        return self.engine

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.engine.dispose()
        logger.info('Disconnected from database')

    def get_db_connection(self) -> Engine:
        try:
            db_user = self.__config['POSTGRES_USER']
            db_port = self.__config['POSTGRES_PORT']
            db_password = self.__config['POSTGRES_PASSWORD']
            db_url = self.__config['POSTGRES_HOST']
            db_name = self.__config['POSTGRES_DB']

            logger.info(f'engine_init[1]: {self.__config}')
            db_connection_string = f'postgresql://{db_user}:{db_password}@{db_url}:{db_port}/{db_name}'

            logger.debug(f'engine_init[2]: connection string is {db_connection_string}')
            return create_engine(db_connection_string)
        except KeyError as e:
            logger.error(f'engine_init[3]: missing environment variable {e}')
            raise KeyError(f'Missing environment variable(s): {e}')
        except Exception as e:
            logger.error(f'engine_init[4]: {e}')
            raise ConnectionError(f'Unable to connect to database: {e}')