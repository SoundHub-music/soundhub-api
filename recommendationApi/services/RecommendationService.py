from typing import Self
from uuid import UUID

from dotenv import dotenv_values
from pandas import DataFrame
from sklearn.neighbors import NearestNeighbors
from sklearn.preprocessing import MultiLabelBinarizer

from exceptions.UserNotFoundException import UserNotFoundException
from repository import GenreRepository
from utils import logger

class RecommendationService:
	instance: Self | None = None

	def __init__(self, genre_repository: GenreRepository) -> None:
		self.__config: dict[str, str | None] = dotenv_values()
		self.__genre_repository = genre_repository
		self.__neighbour_count = int(self.__config['NEIGHBOURS_DEFAULT'])

	def __new__(cls, genre_repository: GenreRepository):
		if cls.instance is None:
			cls.instance = super().__new__(cls)
		return cls.instance

	def __find_nearest_neighbours(self, favorite_genres: DataFrame, user_id: UUID):
		# Преобразуем preferredGenres в формат, пригодный для машинного обучения
		mlb = MultiLabelBinarizer()
		genres_encoded = mlb.fit_transform(favorite_genres['genre_id'])

		knn: NearestNeighbors = NearestNeighbors(
			n_neighbors=self.__neighbour_count,
			algorithm='auto',
			metric = 'cosine',
		).fit(genres_encoded)

		user_index = favorite_genres.index[favorite_genres['user_id'] == user_id]

		# Получаем индексы и расстояния до ближайших соседей
		distances, indices = knn.kneighbors(genres_encoded[user_index])

		# Извлекаем userId ближайших соседей
		neighbors_user_ids = favorite_genres.iloc[indices[0]]['user_id'].tolist()

		if user_id in neighbors_user_ids:
			neighbors_user_ids.remove(user_id)

		return neighbors_user_ids

	def find_potential_friends(self, user_id: UUID) -> list[UUID]:
		try:
			# Группируем по user_id и собираем genres_id в список
			favorite_genres = self.__genre_repository.get_favorite_genres_grouped_by_users()
			registered_user_count: int = len(favorite_genres.index)

			if user_id not in favorite_genres['user_id'].values:
				raise UserNotFoundException(user_id)

			if self.__neighbour_count > registered_user_count:
				self.__neighbour_count = registered_user_count

			potential_friend_ids = self.__find_nearest_neighbours(favorite_genres, user_id)

			logger.debug(f"potential_friends[1]: {potential_friend_ids}")
			return potential_friend_ids

		except Exception as e:
			logger.error(f"potential_friends[2]: {e}")
			raise e