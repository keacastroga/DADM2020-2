package com.example.reto8


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.widget.SearchView

class MainActivity : AppCompatActivity() {

    private lateinit var contactList: ListView
    private lateinit var arrayAdapter: ArrayAdapter<Contact>
    lateinit var mydb: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mydb = DBHelper(this)
        arrayAdapter = getSelectedContactsAdapter(0)
        contactList = findViewById(R.id.listView1)
        contactList.adapter = arrayAdapter
        contactList.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val idToSearch = position + 1
                val dataBundle = Bundle()
                dataBundle.putInt("id", idToSearch)
                val intent = Intent(applicationContext, DisplayContact::class.java)
                intent.putExtras(dataBundle)
                startActivity(intent)
            }

        findViewById<SearchView>(R.id.search_bar).setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                this@MainActivity.arrayAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                this@MainActivity.arrayAdapter.filter.filter(newText)
                return false
            }
        })
        val classificationSpinner = findViewById<Spinner>(R.id.classification)
        ArrayAdapter.createFromResource(
            this,
            R.array.search_array,
            android.R.layout.simple_spinner_item
        ).also{adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            classificationSpinner.adapter = adapter
        }
        classificationSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                arrayAdapter = getSelectedContactsAdapter(position)
                contactList.adapter = arrayAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when(item.itemId){
            R.id.New_Contact -> {
                val dataBundle = Bundle()
                dataBundle.putInt("id", 0)
                val intent = Intent(applicationContext, DisplayContact::class.java)
                intent.putExtras(dataBundle)
                startActivity(intent)
                true
            }else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true)
        }
        return super.onKeyDown(keyCode, event)
    }

    fun getSelectedContactsAdapter(classificationId: Int):ArrayAdapter<Contact> {
        val arrayList = mydb.allContacts
        return if(classificationId == 0){
            ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, arrayList)
        }else{
            val filteredList = ArrayList<Contact>()
            for (contact in arrayList){
                if(contact.classification == (classificationId-1)){
                    filteredList.add(contact)
                }
            }
            ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, filteredList)
        }
    }
}
