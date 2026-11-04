package com.example.budgetmate.data

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val openingBalance: Long = 0
)

@Entity(
    foreignKeys = [ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("accountId"), Index("parentId")]
)
data class Envelope(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val parentId: Long? = null,
    val name: String,
    val rolloverToSavings: Boolean = false,
    val isSavings: Boolean = false
)

@Entity(
    foreignKeys = [
        ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Envelope::class, parentColumns = ["id"], childColumns = ["envelopeId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("accountId"), Index("envelopeId")]
)
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val envelopeId: Long? = null,
    val type: String, // INCOME, ALLOCATION, EXPENSE, TRANSFER, ROLLOVER
    val amount: Long, // positive inflow/expense amount; allocations may be signed
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val transferAccountId: Long? = null
)

@Entity
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Long,
    val savedAmount: Long = 0,
    val dueDate: Long? = null,
    val note: String = ""
)

@Entity
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Long,
    val type: String, // INCOME or EXPENSE
    val accountId: Long,
    val envelopeId: Long? = null,
    val interval: String = "MONTHLY",
    val nextDue: Long,
    val active: Boolean = true
)

@Dao
interface BudgetDao {
    @Query("SELECT * FROM Account ORDER BY id") fun accounts(): Flow<List<Account>>
    @Query("SELECT * FROM Envelope ORDER BY id") fun envelopes(): Flow<List<Envelope>>
    @Query("SELECT * FROM LedgerEntry ORDER BY date DESC, id DESC") fun entries(): Flow<List<LedgerEntry>>
    @Query("SELECT * FROM SavingsGoal ORDER BY id DESC") fun goals(): Flow<List<SavingsGoal>>
    @Query("SELECT * FROM RecurringTransaction ORDER BY nextDue") fun recurring(): Flow<List<RecurringTransaction>>

    @Insert suspend fun addAccount(account: Account): Long
    @Insert suspend fun addEnvelope(envelope: Envelope): Long
    @Insert suspend fun addEntry(entry: LedgerEntry): Long
    @Insert suspend fun addGoal(goal: SavingsGoal): Long
    @Insert suspend fun addRecurring(item: RecurringTransaction): Long

    @Update suspend fun updateAccount(account: Account)
    @Update suspend fun updateEnvelope(envelope: Envelope)
    @Update suspend fun updateEntry(entry: LedgerEntry)
    @Update suspend fun updateGoal(goal: SavingsGoal)
    @Update suspend fun updateRecurring(item: RecurringTransaction)

    @Delete suspend fun deleteAccount(account: Account)
    @Delete suspend fun deleteEnvelope(envelope: Envelope)
    @Delete suspend fun deleteGoal(goal: SavingsGoal)
    @Delete suspend fun deleteRecurring(item: RecurringTransaction)
    @Delete suspend fun deleteEntry(entry: LedgerEntry)

    @Query("SELECT * FROM Account WHERE id = :id") suspend fun account(id: Long): Account?
    @Query("SELECT * FROM Envelope WHERE id = :id") suspend fun envelope(id: Long): Envelope?
    @Query("SELECT * FROM Envelope WHERE parentId = :id") suspend fun children(id: Long): List<Envelope>
    @Query("SELECT * FROM RecurringTransaction WHERE active = 1 AND nextDue <= :now") suspend fun dueRecurring(now: Long): List<RecurringTransaction>
}

@Database(
    entities = [Account::class, Envelope::class, LedgerEntry::class, SavingsGoal::class, RecurringTransaction::class],
    version = 2,
    exportSchema = false
)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun dao(): BudgetDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS SavingsGoal (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, targetAmount INTEGER NOT NULL, savedAmount INTEGER NOT NULL, dueDate INTEGER, note TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS RecurringTransaction (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, amount INTEGER NOT NULL, type TEXT NOT NULL, accountId INTEGER NOT NULL, envelopeId INTEGER, interval TEXT NOT NULL, nextDue INTEGER NOT NULL, active INTEGER NOT NULL)")
            }
        }
    }
}
