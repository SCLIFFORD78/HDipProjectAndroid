package ie.wit.hive.models

interface UserStore {
    suspend fun findAll(): List<UserModel>
    suspend fun create(user: UserModel)
    suspend fun delete(user: UserModel)
    suspend fun clear()
}