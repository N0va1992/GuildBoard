package com.example.guildboard

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GuildBoardDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "guildboard_local_v5.db"
        private const val DATABASE_VERSION = 8 // Podbijamy wersję dla wymuszenia poprawnej tabeli guild_games
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE cities (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL UNIQUE,
              latitude REAL NOT NULL,
              longitude REAL NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE categories (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL UNIQUE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE games (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              title TEXT NOT NULL UNIQUE,
              category_id INTEGER NOT NULL REFERENCES categories(id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE users (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              nickname TEXT NOT NULL UNIQUE,
              email TEXT NOT NULL UNIQUE,
              password_hash TEXT NOT NULL,
              role TEXT NOT NULL DEFAULT 'user',
              level INTEGER NOT NULL DEFAULT 1,
              city_id INTEGER REFERENCES cities(id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE user_games (
              user_id INTEGER REFERENCES users(id),
              game_id INTEGER REFERENCES games(id),
              PRIMARY KEY (user_id, game_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE guilds (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL UNIQUE,
              description TEXT,
              owner_id INTEGER NOT NULL UNIQUE REFERENCES users(id),
              city_id INTEGER REFERENCES cities(id),
              requires_approval INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE guild_members (
              user_id INTEGER REFERENCES users(id),
              guild_id INTEGER NOT NULL REFERENCES guilds(id),
              role TEXT NOT NULL DEFAULT 'member',
              status TEXT NOT NULL DEFAULT 'active',
              PRIMARY KEY (user_id, guild_id)
            )
        """.trimIndent())

        // NAPRAWA: Zmiana tabeli guild_games tak by nie powodowała SQLiteException: no such column gg.id
        db.execSQL("""
            CREATE TABLE guild_games (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              guild_id INTEGER REFERENCES guilds(id),
              game_id INTEGER REFERENCES games(id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE game_events (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              title TEXT NOT NULL,
              description TEXT,
              max_seats INTEGER NOT NULL,
              game_id INTEGER NOT NULL REFERENCES games(id),
              category_id INTEGER NOT NULL REFERENCES categories(id),
              guild_id INTEGER REFERENCES guilds(id),
              organizer_id INTEGER NOT NULL REFERENCES users(id),
              city_id INTEGER REFERENCES cities(id),
              address TEXT,
              latitude REAL,
              longitude REAL,
              starts_at TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE event_participants (
              event_id INTEGER REFERENCES game_events(id),
              user_id INTEGER REFERENCES users(id),
              event_role TEXT,
              commitment TEXT,
              joined_at TEXT NOT NULL,
              PRIMARY KEY (event_id, user_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE event_chat_messages (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              event_id INTEGER NOT NULL REFERENCES game_events(id),
              sender_id INTEGER REFERENCES users(id),
              message_text TEXT NOT NULL,
              created_at TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE guild_chat_messages (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              guild_id INTEGER NOT NULL REFERENCES guilds(id),
              sender_id INTEGER REFERENCES users(id),
              message_text TEXT NOT NULL,
              created_at TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE user_ratings (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              rated_user_id INTEGER NOT NULL REFERENCES users(id),
              rater_user_id INTEGER NOT NULL REFERENCES users(id),
              score INTEGER NOT NULL,
              created_at TEXT NOT NULL,
              UNIQUE (rated_user_id, rater_user_id)
            )
        """.trimIndent())

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val tables = listOf("user_ratings", "guild_chat_messages", "event_chat_messages", "event_participants", "game_events", "guild_games", "guild_members", "guilds", "user_games", "users", "games", "categories", "cities")
        tables.forEach { db.execSQL("DROP TABLE IF EXISTS $it") }
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val catBoard = insertCategory(db, "Planszówki")
        val catRpg = insertCategory(db, "Sesje RPG")
        val catTour = insertCategory(db, "Turnieje")
        
        val cityKrakow = insertCity(db, "Kraków", 50.0647, 19.9450)
        
        val gameNemesis = insertGame(db, "Nemesis Lockdown", catBoard)
        val gameCatan = insertGame(db, "Catan: Miasta i Rycerze", catBoard)
        val gameTerraforming = insertGame(db, "Terraforming Mars", catBoard)
        val gameGloomhaven = insertGame(db, "Gloomhaven: Szczęki Lewiatana", catBoard)
        val gameBrass = insertGame(db, "Brass Birmingham", catBoard)
        val gameEverdell = insertGame(db, "Everdell", catBoard)
        val gameWingspan = insertGame(db, "Wingspan", catBoard)
        val gameDnD = insertGame(db, "Dungeons & Dragons 5e", catRpg)
        val gameCoC = insertGame(db, "Call of Cthulhu", catRpg)
        val gameMagic = insertGame(db, "Magic: The Gathering", catTour)

        db.execSQL("INSERT INTO users (nickname, email, password_hash, role, level, city_id) VALUES ('Jan_Gracz', 'jan@example.com', 'Password123', 'admin', 5, $cityKrakow)")
        db.execSQL("INSERT INTO users (nickname, email, password_hash, role, level, city_id) VALUES ('Piotr_RPG', 'piotr@example.com', 'Password123', 'moderator', 8, $cityKrakow)")
        db.execSQL("INSERT INTO users (nickname, email, password_hash, role, level, city_id) VALUES ('Ania_Boardgames', 'ania@example.com', 'Password123', 'user', 3, $cityKrakow)")

        val uJan = 1L
        val uPiotr = 2L
        val uAnia = 3L

        db.execSQL("INSERT INTO user_games (user_id, game_id) VALUES ($uJan, $gameNemesis), ($uJan, $gameCatan), ($uJan, $gameTerraforming)")
        db.execSQL("INSERT INTO user_games (user_id, game_id) VALUES ($uPiotr, $gameNemesis), ($uPiotr, $gameGloomhaven), ($uPiotr, $gameBrass)")
        db.execSQL("INSERT INTO user_games (user_id, game_id) VALUES ($uAnia, $gameCatan), ($uAnia, $gameEverdell), ($uAnia, $gameWingspan)")

        db.execSQL("INSERT INTO user_ratings (rated_user_id, rater_user_id, score, created_at) VALUES ($uJan, $uPiotr, 5, '2023-10-01'), ($uPiotr, $uJan, 5, '2023-10-01'), ($uAnia, $uJan, 4, '2023-10-01')")

        db.execSQL("INSERT INTO game_events (title, description, max_seats, game_id, category_id, organizer_id, city_id, address, starts_at) VALUES ('Nemesis Lockdown', 'Surwiwal horror z figurkami. Szukamy 2 doświadczonych graczy.', 5, $gameNemesis, $catBoard, $uJan, $cityKrakow, 'Klub Bardo, ul. Batorego 15', '2023-10-20 18:00')")
        val evNemesis = 1L
        db.execSQL("INSERT INTO game_events (title, description, max_seats, game_id, category_id, organizer_id, city_id, address, starts_at) VALUES ('Catan: Miasta i Rycerze', 'Szybka rozgrywka dla każdego. Zapewniamy przekąski!', 4, $gameCatan, $catBoard, $uAnia, $cityKrakow, 'Kawiarnia Hex, ul. Dwernickiego', '2023-10-21 16:00')")
        val evCatan = 2L
        db.execSQL("INSERT INTO game_events (title, description, max_seats, game_id, category_id, organizer_id, city_id, address, starts_at) VALUES ('Dungeons & Dragons 5e - Klątwa Strahda', 'Mroczna kampania RPG. Szukamy gracza do stałej grupy.', 5, $gameDnD, $catRpg, $uPiotr, $cityKrakow, 'Kraków, ul. Długa 20/4', '2023-10-22 16:00')")
        val evDnd = 3L
        db.execSQL("INSERT INTO game_events (title, description, max_seats, game_id, category_id, organizer_id, city_id, address, starts_at) VALUES ('Turniej Magic: The Gathering (Pauper)', 'Lokalny turniej w sklepie. Nagrody dla top 3!', 16, $gameMagic, $catTour, $uPiotr, $cityKrakow, 'Sklep Vanaheim', '2023-10-25 16:00')")
        val evMagic = 4L

        db.execSQL("INSERT INTO event_participants (event_id, user_id, event_role, commitment, joined_at) VALUES ($evNemesis, $uJan, 'Organizator', 'Na pewno', '2023-10-01'), ($evNemesis, $uPiotr, 'Gracz', 'Na pewno', '2023-10-02'), ($evNemesis, $uAnia, 'Obserwator', 'Tylko obserwuję', '2023-10-03')")
        db.execSQL("INSERT INTO event_participants (event_id, user_id, event_role, commitment, joined_at) VALUES ($evCatan, $uAnia, 'Mistrz Gry', 'Na pewno', '2023-10-01'), ($evCatan, $uJan, 'Gracz', 'Może będę', '2023-10-02')")

        db.execSQL("INSERT INTO event_chat_messages (event_id, sender_id, message_text, created_at) VALUES ($evNemesis, NULL, 'Połączono ze stolikiem: Nemesis Lockdown', '17:00')")
        db.execSQL("INSERT INTO event_chat_messages (event_id, sender_id, message_text, created_at) VALUES ($evNemesis, $uJan, 'Cześć wszystkim! Potwierdzamy grę w piątek o 18:00?', '17:05')")

        db.execSQL("INSERT INTO guilds (name, description, owner_id, city_id, requires_approval) VALUES ('Krakowska Gildia Planszówkowa', 'Oficjalna grupa graczy z Krakowa. Spotkania w każdy piątek.', $uJan, $cityKrakow, 0)")
        val gKrakow = 1L
        db.execSQL("INSERT INTO guilds (name, description, owner_id, city_id, requires_approval) VALUES ('RPG Małopolska - Mistrzowie Mroku', 'Zamknięta grupa dla graczy i Mistrzów Gry systemów D&D, CoC oraz Neuroshima.', $uPiotr, $cityKrakow, 1)")
        val gRpg = 2L

        db.execSQL("INSERT INTO guild_members (user_id, guild_id, role, status) VALUES ($uJan, $gKrakow, 'owner', 'active')")
        db.execSQL("INSERT INTO guild_members (user_id, guild_id, role, status) VALUES ($uAnia, $gKrakow, 'moderator', 'active')")
        db.execSQL("INSERT INTO guild_members (user_id, guild_id, role, status) VALUES ($uPiotr, $gRpg, 'owner', 'active')")

        db.execSQL("INSERT INTO guild_chat_messages (guild_id, sender_id, message_text, created_at) VALUES ($gKrakow, NULL, 'Witaj na czacie Krakowskiej Gildii Planszówkowej!', '12:00')")
        db.execSQL("INSERT INTO guild_chat_messages (guild_id, sender_id, message_text, created_at) VALUES ($gKrakow, $uJan, 'Cześć wszystkim! Co przynosicie na najbliższe spotkanie?', '12:05')")
        db.execSQL("INSERT INTO guild_chat_messages (guild_id, sender_id, message_text, created_at) VALUES ($gKrakow, $uAnia, 'Siema! Wezmę Everdell i Catan z dodatkami.', '12:10')")

        db.execSQL("INSERT INTO guild_games (guild_id, game_id) VALUES ($gKrakow, $gameCatan), ($gKrakow, $gameNemesis), ($gRpg, $gameDnD), ($gRpg, $gameCoC)")
    }

    private fun insertCity(db: SQLiteDatabase, name: String, lat: Double, lng: Double): Long {
        val values = ContentValues().apply { put("name", name); put("latitude", lat); put("longitude", lng) }
        return db.insert("cities", null, values)
    }
    
    private fun insertCategory(db: SQLiteDatabase, name: String): Long {
        val values = ContentValues().apply { put("name", name) }
        return db.insert("categories", null, values)
    }
    
    private fun insertGame(db: SQLiteDatabase, title: String, categoryId: Long): Long {
        val values = ContentValues().apply { put("title", title); put("category_id", categoryId) }
        return db.insert("games", null, values)
    }

    private fun getOrCreateCity(db: SQLiteDatabase, cityName: String): Long {
        val cursor = db.rawQuery("SELECT id FROM cities WHERE name = ?", arrayOf(cityName))
        if (cursor.moveToFirst()) { val id = cursor.getLong(0); cursor.close(); return id }
        cursor.close()
        return insertCity(db, cityName, 50.0, 20.0)
    }

    private fun getOrCreateCategory(db: SQLiteDatabase, categoryName: String): Long {
        val cursor = db.rawQuery("SELECT id FROM categories WHERE name = ?", arrayOf(categoryName))
        if (cursor.moveToFirst()) { val id = cursor.getLong(0); cursor.close(); return id }
        cursor.close()
        return insertCategory(db, categoryName)
    }

    private fun getOrCreateGame(db: SQLiteDatabase, title: String, categoryId: Long): Long {
        val cursor = db.rawQuery("SELECT id FROM games WHERE title = ?", arrayOf(title))
        if (cursor.moveToFirst()) { val id = cursor.getLong(0); cursor.close(); return id }
        cursor.close()
        return insertGame(db, title, categoryId)
    }

    fun isUserExists(nickname: String, email: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM users WHERE nickname = ? OR email = ?", arrayOf(nickname, email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun registerUser(user: User): Long {
        if (isUserExists(user.nickname, user.email)) return -1L
        val db = writableDatabase
        val cityId = getOrCreateCity(db, user.city.ifEmpty { "Kraków" })

        val values = ContentValues().apply {
            put("nickname", user.nickname)
            put("email", user.email)
            put("password_hash", user.password)
            put("role", if (user.roleAndLevel.contains("Mistrz Gry") || user.roleAndLevel.contains("Organizator")) "moderator" else "user")
            put("level", 1)
            put("city_id", cityId)
        }
        val userId = db.insert("users", null, values)
        
        if (userId > 0 && user.ownedGamesText.isNotBlank()) {
            val games = user.ownedGamesText.split(",").map { it.trim() }
            val catId = getOrCreateCategory(db, "Planszówki")
            games.forEach { g ->
                val gameId = getOrCreateGame(db, g, catId)
                val ugVals = ContentValues().apply { put("user_id", userId); put("game_id", gameId) }
                db.insertWithOnConflict("user_games", null, ugVals, SQLiteDatabase.CONFLICT_IGNORE)
            }
        }
        return userId
    }

    fun authenticateUser(emailOrNick: String, pass: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM users WHERE (nickname = ? OR email = ?) AND password_hash = ?", arrayOf(emailOrNick, emailOrNick, pass))
        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            user = getUserById(id)
        }
        cursor.close()
        return user
    }

    fun getUserByNick(nick: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM users WHERE nickname = ?", arrayOf(nick))
        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            user = getUserById(id)
        }
        cursor.close()
        return user
    }

    fun getUserById(id: Long): User? {
        val db = readableDatabase
        val query = """
            SELECT u.id, u.nickname, u.email, u.password_hash, u.role, u.level, c.name as city_name,
                   (SELECT GROUP_CONCAT(g.title, ', ') FROM user_games ug JOIN games g ON ug.game_id = g.id WHERE ug.user_id = u.id) as owned_games,
                   (SELECT COUNT(*) FROM event_participants ep WHERE ep.user_id = u.id) as games_played,
                   (SELECT COUNT(*) FROM game_events ge WHERE ge.organizer_id = u.id) as tables_organized,
                   (SELECT AVG(score) FROM user_ratings ur WHERE ur.rated_user_id = u.id) as rating
            FROM users u
            LEFT JOIN cities c ON u.city_id = c.id
            WHERE u.id = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(id.toString()))
        var user: User? = null
        if (cursor.moveToFirst()) {
            val nick = cursor.getString(cursor.getColumnIndexOrThrow("nickname"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val pass = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"))
            val role = cursor.getString(cursor.getColumnIndexOrThrow("role"))
            val level = cursor.getInt(cursor.getColumnIndexOrThrow("level"))
            val city = cursor.getString(cursor.getColumnIndexOrThrow("city_name")) ?: "Kraków"
            val owned = cursor.getString(cursor.getColumnIndexOrThrow("owned_games")) ?: ""
            val gPlayed = cursor.getInt(cursor.getColumnIndexOrThrow("games_played"))
            val tOrg = cursor.getInt(cursor.getColumnIndexOrThrow("tables_organized"))
            val rating = cursor.getDouble(cursor.getColumnIndexOrThrow("rating"))
            
            val roleLevelStr = if (role == "moderator" || role == "admin") "Organizator / MG • Poziom $level" else "Gracz • Poziom $level"
            user = User(id, nick, email, pass, roleLevelStr, city, "$city (1.0 km)", owned, gPlayed, tOrg, rating)
        }
        cursor.close()
        return user
    }

    fun updateUserProfile(userId: Long, newNick: String, newCity: String, newGames: String, newRole: String): Boolean {
        val db = writableDatabase
        val cityId = getOrCreateCity(db, newCity)
        val values = ContentValues().apply {
            put("nickname", newNick)
            put("city_id", cityId)
            put("role", if (newRole.contains("Mistrz") || newRole.contains("Organizator")) "moderator" else "user")
        }
        val rows = db.update("users", values, "id = ?", arrayOf(userId.toString()))
        
        db.delete("user_games", "user_id = ?", arrayOf(userId.toString()))
        if (newGames.isNotBlank()) {
            val games = newGames.split(",").map { it.trim() }
            val catId = getOrCreateCategory(db, "Planszówki")
            games.forEach { g ->
                val gameId = getOrCreateGame(db, g, catId)
                val ugVals = ContentValues().apply { put("user_id", userId); put("game_id", gameId) }
                db.insertWithOnConflict("user_games", null, ugVals, SQLiteDatabase.CONFLICT_IGNORE)
            }
        }
        return rows > 0
    }

    fun getAllPlayers(): List<PlayerProfile> {
        val db = readableDatabase
        val query = """
            SELECT u.id, u.nickname, u.role, u.level, c.name as city_name,
                   (SELECT GROUP_CONCAT(g.title, ', ') FROM user_games ug JOIN games g ON ug.game_id = g.id WHERE ug.user_id = u.id) as owned_games
            FROM users u
            LEFT JOIN cities c ON u.city_id = c.id
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        val list = mutableListOf<PlayerProfile>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id")).toString()
            val nick = cursor.getString(cursor.getColumnIndexOrThrow("nickname"))
            val role = cursor.getString(cursor.getColumnIndexOrThrow("role"))
            val level = cursor.getInt(cursor.getColumnIndexOrThrow("level"))
            val city = cursor.getString(cursor.getColumnIndexOrThrow("city_name")) ?: "Kraków"
            val games = cursor.getString(cursor.getColumnIndexOrThrow("owned_games")) ?: ""
            
            val roleStr = if (role == "moderator" || role == "admin") "Organizator / MG • Poz. $level" else "Gracz • Poz. $level"
            val gamesText = if (games.isNotEmpty()) "Posiadam: $games" else "Brak podanych gier"
            list.add(PlayerProfile(id, nick, roleStr, "$city (1.0 km)", gamesText))
        }
        cursor.close()
        return list
    }

    // --- STOŁY I CZAT STOŁU ---
    fun getAllGameEvents(currentUserId: Long? = null): List<GameEvent> {
        val db = readableDatabase
        val arg = currentUserId?.toString() ?: "-1"
        val query = """
            SELECT ge.id, ge.title, ge.description, ge.max_seats, ge.starts_at, ge.address,
                   u.id as org_id, u.nickname as org_nick,
                   cat.name as category_name, c.name as city_name,
                   (SELECT COUNT(*) FROM event_participants ep WHERE ep.event_id = ge.id AND ep.commitment != 'Tylko obserwuję') as current_seats,
                   (SELECT 1 FROM event_participants ep WHERE ep.event_id = ge.id AND ep.user_id = ?) as is_joined,
                   (SELECT commitment FROM event_participants ep WHERE ep.event_id = ge.id AND ep.user_id = ?) as user_commitment,
                   (SELECT event_role FROM event_participants ep WHERE ep.event_id = ge.id AND ep.user_id = ?) as user_event_role
            FROM game_events ge
            JOIN users u ON ge.organizer_id = u.id
            JOIN categories cat ON ge.category_id = cat.id
            LEFT JOIN cities c ON ge.city_id = c.id
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(arg, arg, arg))
        val list = mutableListOf<GameEvent>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id")).toString()
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: ""
            val maxSeats = cursor.getInt(cursor.getColumnIndexOrThrow("max_seats"))
            val orgId = cursor.getLong(cursor.getColumnIndexOrThrow("org_id"))
            val orgNick = cursor.getString(cursor.getColumnIndexOrThrow("org_nick"))
            val category = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
            val city = cursor.getString(cursor.getColumnIndexOrThrow("city_name")) ?: "Kraków"
            val address = cursor.getString(cursor.getColumnIndexOrThrow("address")) ?: ""
            val startsAt = cursor.getString(cursor.getColumnIndexOrThrow("starts_at"))
            val currSeats = cursor.getInt(cursor.getColumnIndexOrThrow("current_seats"))
            val joined = cursor.getInt(cursor.getColumnIndexOrThrow("is_joined")) == 1
            val userCommitment = cursor.getString(cursor.getColumnIndexOrThrow("user_commitment")) ?: ""
            val userEventRole = cursor.getString(cursor.getColumnIndexOrThrow("user_event_role")) ?: ""

            list.add(GameEvent(id, title, desc, 3.0, currSeats, maxSeats, category, joined, orgId, orgNick, city, address, startsAt, userCommitment, userEventRole))
        }
        cursor.close()
        return list
    }

    fun createGameEvent(title: String, desc: String, maxSeats: Int, orgId: Long, city: String, address: String, startsAt: String): Long {
        val db = writableDatabase
        val cityId = getOrCreateCity(db, city)
        val catId = getOrCreateCategory(db, "Planszówki")
        val gameId = getOrCreateGame(db, title, catId)

        val values = ContentValues().apply {
            put("title", title)
            put("description", desc)
            put("max_seats", maxSeats)
            put("game_id", gameId)
            put("category_id", catId)
            put("organizer_id", orgId)
            put("city_id", cityId)
            put("address", address)
            put("starts_at", startsAt)
        }
        val eventId = db.insert("game_events", null, values)
        
        if (eventId > 0) {
            joinOrUpdateEventParticipant(eventId, orgId, "Organizator / MG", "Na pewno będę")
        }
        return eventId
    }

    fun joinOrUpdateEventParticipant(eventId: Long, userId: Long, role: String, commitment: String) {
        val db = writableDatabase
        val timeNow = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put("event_id", eventId)
            put("user_id", userId)
            put("event_role", role)
            put("commitment", commitment)
            put("joined_at", timeNow)
        }
        db.insertWithOnConflict("event_participants", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getChatMessages(eventId: Int = 1): List<ChatMessage> {
        val db = readableDatabase
        val query = """
            SELECT cm.id, cm.message_text, cm.created_at, cm.sender_id, u.nickname
            FROM event_chat_messages cm
            LEFT JOIN users u ON cm.sender_id = u.id
            WHERE cm.event_id = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(eventId.toString()))
        val list = mutableListOf<ChatMessage>()
        while (cursor.moveToNext()) {
            val senderId = cursor.isNull(cursor.getColumnIndexOrThrow("sender_id"))
            val nick = if (senderId) "System" else cursor.getString(cursor.getColumnIndexOrThrow("nickname"))
            val text = cursor.getString(cursor.getColumnIndexOrThrow("message_text"))
            val time = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            
            list.add(ChatMessage(nick, time, text, senderId))
        }
        cursor.close()
        return list
    }

    fun addChatMessage(eventId: Int, userId: Long, messageText: String): Long {
        val db = writableDatabase
        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put("event_id", eventId)
            put("sender_id", userId)
            put("message_text", messageText)
            put("created_at", timeNow)
        }
        return db.insert("event_chat_messages", null, values)
    }

    // --- GILDIE ---
    fun getAllGuilds(): List<Guild> {
        val db = readableDatabase
        val query = """
            SELECT g.id, g.name, g.description, g.owner_id, u.nickname as owner_nick, c.name as city_name, g.requires_approval,
                   (SELECT COUNT(*) FROM guild_members gm WHERE gm.guild_id = g.id AND gm.status = 'active') as member_count
            FROM guilds g
            JOIN users u ON g.owner_id = u.id
            LEFT JOIN cities c ON g.city_id = c.id
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        val list = mutableListOf<Guild>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: ""
            val ownerId = cursor.getLong(cursor.getColumnIndexOrThrow("owner_id"))
            val ownerNick = cursor.getString(cursor.getColumnIndexOrThrow("owner_nick"))
            val city = cursor.getString(cursor.getColumnIndexOrThrow("city_name")) ?: "Kraków"
            val reqApp = cursor.getInt(cursor.getColumnIndexOrThrow("requires_approval")) == 1
            val count = cursor.getInt(cursor.getColumnIndexOrThrow("member_count"))

            list.add(Guild(id, name, desc, ownerId, ownerNick, city, reqApp, count))
        }
        cursor.close()
        return list
    }

    fun getUserGuild(userId: Long): Guild? {
        val db = readableDatabase
        val query = """
            SELECT g.id, g.name, g.description, g.owner_id, u.nickname as owner_nick, c.name as city_name, g.requires_approval,
                   (SELECT COUNT(*) FROM guild_members gm2 WHERE gm2.guild_id = g.id AND gm2.status = 'active') as member_count
            FROM guilds g
            JOIN users u ON g.owner_id = u.id
            LEFT JOIN cities c ON g.city_id = c.id
            INNER JOIN guild_members gm ON g.id = gm.guild_id
            WHERE gm.user_id = ? AND gm.status = 'active'
            LIMIT 1
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var guild: Guild? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: ""
            val ownerId = cursor.getLong(cursor.getColumnIndexOrThrow("owner_id"))
            val ownerNick = cursor.getString(cursor.getColumnIndexOrThrow("owner_nick"))
            val city = cursor.getString(cursor.getColumnIndexOrThrow("city_name")) ?: "Kraków"
            val reqApp = cursor.getInt(cursor.getColumnIndexOrThrow("requires_approval")) == 1
            val count = cursor.getInt(cursor.getColumnIndexOrThrow("member_count"))

            guild = Guild(id, name, desc, ownerId, ownerNick, city, reqApp, count)
        }
        cursor.close()
        return guild
    }

    fun getUserGuildRole(guildId: Long, userId: Long): String? {
        val db = readableDatabase
        val cursorG = db.rawQuery("SELECT owner_id FROM guilds WHERE id = ?", arrayOf(guildId.toString()))
        var ownerId: Long = -1
        if (cursorG.moveToFirst()) { ownerId = cursorG.getLong(0) }
        cursorG.close()
        
        if (ownerId == userId) return "OWNER"

        val cursor = db.rawQuery("SELECT role FROM guild_members WHERE guild_id = ? AND user_id = ? AND status = 'active'", arrayOf(guildId.toString(), userId.toString()))
        var role: String? = null
        if (cursor.moveToFirst()) {
            val dbRole = cursor.getString(0)
            role = dbRole.uppercase()
        }
        cursor.close()
        return role
    }

    fun isUserInAnyGuild(userId: Long): Boolean {
        return getUserGuild(userId) != null
    }

    fun createGuild(guild: Guild): Long {
        if (isUserInAnyGuild(guild.ownerId)) return -1L

        val db = writableDatabase
        val cityId = getOrCreateCity(db, guild.city)
        val values = ContentValues().apply {
            put("name", guild.name)
            put("description", guild.description)
            put("owner_id", guild.ownerId)
            put("city_id", cityId)
            put("requires_approval", if (guild.requiresApproval) 1 else 0)
        }
        val guildId = db.insert("guilds", null, values)

        if (guildId > 0) {
            db.delete("guild_members", "user_id = ? AND status = 'pending'", arrayOf(guild.ownerId.toString()))
            val memberValues = ContentValues().apply {
                put("guild_id", guildId)
                put("user_id", guild.ownerId)
                put("role", "owner")
                put("status", "active")
            }
            db.insertWithOnConflict("guild_members", null, memberValues, SQLiteDatabase.CONFLICT_REPLACE)
        }
        return guildId
    }

    fun joinGuild(guildId: Long, userId: Long, requiresApproval: Boolean): String {
        if (isUserInAnyGuild(userId)) return "ALREADY_IN_GUILD"
        val db = writableDatabase
        
        val status = if (requiresApproval) "pending" else "active"
        val values = ContentValues().apply {
            put("guild_id", guildId)
            put("user_id", userId)
            put("role", "member")
            put("status", status)
        }
        db.insertWithOnConflict("guild_members", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        
        if (status == "active") {
            db.delete("guild_members", "user_id = ? AND status = 'pending' AND guild_id != ?", arrayOf(userId.toString(), guildId.toString()))
        }
        
        return status.uppercase()
    }

    fun getGuildMembers(guildId: Long): List<GuildMember> {
        val db = readableDatabase
        val query = """
            SELECT gm.user_id, u.nickname, gm.role, gm.status, g.owner_id
            FROM guild_members gm
            JOIN users u ON gm.user_id = u.id
            JOIN guilds g ON gm.guild_id = g.id
            WHERE gm.guild_id = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(guildId.toString()))
        val list = mutableListOf<GuildMember>()
        
        while (cursor.moveToNext()) {
            val uId = cursor.getLong(cursor.getColumnIndexOrThrow("user_id"))
            val nick = cursor.getString(cursor.getColumnIndexOrThrow("nickname"))
            val roleDb = cursor.getString(cursor.getColumnIndexOrThrow("role"))
            val statusDb = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            val ownerId = cursor.getLong(cursor.getColumnIndexOrThrow("owner_id"))
            
            val calculatedRole = if (uId == ownerId) "OWNER" else roleDb.uppercase()
            val status = statusDb.uppercase()

            list.add(GuildMember(uId, guildId, uId, nick, calculatedRole, status))
        }
        cursor.close()
        return list
    }

    fun updateGuildMemberRole(guildId: Long, userId: Long, newRole: String) {
        val db = writableDatabase
        val values = ContentValues().apply { put("role", newRole.lowercase()) }
        db.update("guild_members", values, "user_id = ? AND guild_id = ?", arrayOf(userId.toString(), guildId.toString()))
    }

    fun updateGuildMemberStatus(guildId: Long, userId: Long, newStatus: String) {
        val db = writableDatabase
        val statusDb = newStatus.lowercase()
        val values = ContentValues().apply { put("status", statusDb) }
        db.update("guild_members", values, "user_id = ? AND guild_id = ?", arrayOf(userId.toString(), guildId.toString()))

        if (statusDb == "active") {
            db.delete("guild_members", "user_id = ? AND status = 'pending' AND guild_id != ?", arrayOf(userId.toString(), guildId.toString()))
        }
    }

    fun removeGuildMember(guildId: Long, userId: Long) {
        val db = writableDatabase
        db.delete("guild_members", "user_id = ? AND guild_id = ?", arrayOf(userId.toString(), guildId.toString()))
    }

    fun transferGuildOwnership(guildId: Long, newOwnerUserId: Long): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val valuesGuild = ContentValues().apply { put("owner_id", newOwnerUserId) }
            db.update("guilds", valuesGuild, "id = ?", arrayOf(guildId.toString()))
            
            val valuesMem = ContentValues().apply { put("role", "member") }
            db.update("guild_members", valuesMem, "user_id = ? AND guild_id = ?", arrayOf(newOwnerUserId.toString(), guildId.toString()))

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun leaveOrDisbandGuild(guildId: Long, userId: Long): Boolean {
        val db = writableDatabase
        val role = getUserGuildRole(guildId, userId)

        if (role == "OWNER") {
            db.delete("guilds", "id = ?", arrayOf(guildId.toString()))
            db.delete("guild_members", "guild_id = ?", arrayOf(guildId.toString()))
            db.delete("guild_chat_messages", "guild_id = ?", arrayOf(guildId.toString()))
            db.delete("guild_games", "guild_id = ?", arrayOf(guildId.toString()))
            return true
        } else {
            val rows = db.delete("guild_members", "guild_id = ? AND user_id = ?", arrayOf(guildId.toString(), userId.toString()))
            return rows > 0
        }
    }

    fun getGuildChatMessages(guildId: Long): List<ChatMessage> {
        val db = readableDatabase
        val query = """
            SELECT cm.id, cm.message_text, cm.created_at, cm.sender_id, u.nickname
            FROM guild_chat_messages cm
            LEFT JOIN users u ON cm.sender_id = u.id
            WHERE cm.guild_id = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(guildId.toString()))
        val list = mutableListOf<ChatMessage>()
        while (cursor.moveToNext()) {
            val senderId = cursor.isNull(cursor.getColumnIndexOrThrow("sender_id"))
            val nick = if (senderId) "System" else cursor.getString(cursor.getColumnIndexOrThrow("nickname"))
            val text = cursor.getString(cursor.getColumnIndexOrThrow("message_text"))
            val time = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            
            list.add(ChatMessage(nick, time, text, senderId))
        }
        cursor.close()
        return list
    }

    fun addGuildChatMessage(guildId: Long, userId: Long, messageText: String): Long {
        val db = writableDatabase
        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put("guild_id", guildId)
            put("sender_id", userId)
            put("message_text", messageText)
            put("created_at", timeNow)
        }
        return db.insert("guild_chat_messages", null, values)
    }

    fun getGuildGames(guildId: Long): List<GuildGame> {
        val db = readableDatabase
        val query = """
            SELECT gg.id, g.title as game_title, c.name as category_name
            FROM guild_games gg
            JOIN games g ON gg.game_id = g.id
            JOIN categories c ON g.category_id = c.id
            WHERE gg.guild_id = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(guildId.toString()))
        val list = mutableListOf<GuildGame>()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("game_title"))
            val cat = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))

            list.add(GuildGame(id, guildId, title, cat))
        }
        cursor.close()
        return list
    }

    fun addGuildGame(guildId: Long, title: String): Long {
        val db = writableDatabase
        val catId = getOrCreateCategory(db, "Planszówki")
        val gameId = getOrCreateGame(db, title, catId)
        val values = ContentValues().apply {
            put("guild_id", guildId)
            put("game_id", gameId)
        }
        return db.insertWithOnConflict("guild_games", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }
}