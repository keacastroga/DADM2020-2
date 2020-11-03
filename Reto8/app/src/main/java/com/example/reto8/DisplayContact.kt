package com.example.reto8

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class DisplayContact : AppCompatActivity() {

    private lateinit var mydb: DBHelper

    lateinit var nameView: TextView
    lateinit var phoneView: TextView
    lateinit var emailView: TextView
    lateinit var urlView: TextView
    lateinit var productView: TextView
    lateinit var classificationView: Spinner
    var idToUpdate = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_contact)
        nameView = findViewById(R.id.editTextName)
        phoneView = findViewById(R.id.editTextPhone)
        emailView = findViewById(R.id.editTextEmail)
        urlView = findViewById(R.id.editTextURL)
        productView = findViewById(R.id.editTextProduct)
        classificationView = findViewById((R.id.editTextClassification))

        ArrayAdapter.createFromResource(
            this,
            R.array.classification_array,
            android.R.layout.simple_spinner_item
        ).also{adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            classificationView.adapter = adapter
        }

        mydb = DBHelper(this)

        val extras = intent.extras
        if(extras != null){
            val value = extras.getInt("id")

            if(value > 0){
                val rs = mydb.getData(value)
                idToUpdate = value
                rs.moveToFirst()

                val name = rs.getString(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_NAME))
                val phone = rs.getString(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_PHONE))
                val email = rs.getString(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_EMAIL))
                val url = rs.getString(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_URL))
                val products = rs.getString(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_PRODUCT))
                val classification = rs.getInt(rs.getColumnIndex(DBHelper.CONTACTS_COLUMN_CLASSIFICATION))

                if(!rs.isClosed){
                    rs.close()
                }
                val button = findViewById<Button>(R.id.button1)
                button.visibility = View.INVISIBLE

                nameView.text = name as CharSequence
                nameView.isFocusable = false
                nameView.isClickable = false

                phoneView.text = phone as CharSequence
                phoneView.isFocusable = false
                phoneView.isClickable = false

                emailView.text = email as CharSequence
                emailView.isFocusable = false
                emailView.isClickable = false

                urlView.text = url as CharSequence
                urlView.isFocusable = false
                urlView.isClickable = false

                productView.text = products as CharSequence
                productView.isFocusable = false
                productView.isClickable = false

                classificationView.setSelection(classification)
                classificationView.isEnabled = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val extras = intent.extras

        if(extras != null){
            val value = extras.getInt("id")
            if(value>0){
                menuInflater.inflate(R.menu.display_contact, menu)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when(item.itemId){
            R.id.Edit_Contact -> {
                val button = findViewById<Button>(R.id.button1)
                button.visibility = View.VISIBLE

                nameView.isEnabled = true
                nameView.isFocusableInTouchMode = true
                nameView.isClickable = true

                phoneView.isEnabled = true
                phoneView.isFocusableInTouchMode = true
                phoneView.isClickable = true

                emailView.isEnabled = true
                emailView.isFocusableInTouchMode = true
                emailView.isClickable = true

                urlView.isEnabled = true
                urlView.isFocusableInTouchMode = true
                urlView.isClickable = true

                productView.isEnabled = true
                productView.isFocusableInTouchMode = true
                productView.isClickable = true

                classificationView.isEnabled = true
                return true
            }
            R.id.Delete_Contact -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.deleteContact)
                    .setPositiveButton(R.string.yes){_: DialogInterface, _: Int ->
                        mydb.deleteContact(idToUpdate)
                        Toast.makeText(applicationContext, "Borrado con éxito", Toast.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                    }
                    .setNegativeButton(R.string.no){ _: DialogInterface, _: Int -> }
                val d = builder.create()
                d.setTitle("¿Seguro?")
                d.show()
                return true
            }
            else ->{
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun run(view: View){
        val extras = intent.extras
        if(extras != null){
            val value = extras.getInt("id")
            if(value > 0){
                if(mydb.updateContact(idToUpdate, nameView.text.toString(), phoneView.text.toString(),
                        emailView.text.toString(), urlView.text.toString(), productView.text.toString(), classificationView.selectedItemPosition)){
                    Toast.makeText(applicationContext, "Actualizado", Toast.LENGTH_SHORT).show();
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(applicationContext, "No Actualizado", Toast.LENGTH_SHORT).show();
                }
            }else{
                if(mydb.insertContact(nameView.text.toString(), phoneView.text.toString(),
                        emailView.text.toString(), urlView.text.toString(), productView.text.toString(), classificationView.selectedItemPosition)){
                    Toast.makeText(applicationContext, "Creado", Toast.LENGTH_SHORT).show();
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Toast.makeText(applicationContext, "No Creado", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}