package com.homemadeproductsapp.MyStore.AcceptOrders

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.FtsOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.homemadeproductsapp.DB.Product
import com.homemadeproductsapp.DB.Order
import com.homemadeproductsapp.PastOrders.adapter.OrderAdapter

import com.homemadeproductsapp.R

class OrderDetailsFragment : DialogFragment() {
    private lateinit var recyclerViewItems:RecyclerView
    private lateinit var textViewTotal:TextView
    private lateinit var backImageView:ImageView
    private lateinit var  progressBar: ProgressBar
    private lateinit var order:Order

    private  var total=0.00
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
private lateinit var view1:View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
         view1= inflater.inflate(R.layout.fragment_order, container, false)
        progressBar=view1.findViewById(R.id.progressBar)

        return view1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      order= requireArguments().getSerializable("order") as Order
        bindViews()
        setupClickListeners()




        requireActivity()
                .onBackPressedDispatcher
                .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                     dismiss()
                    }
                }
                )
    }


    private fun setupClickListeners() {
        backImageView.setOnClickListener{
            dismiss()
        }
    }


    override fun onResume() {
        progressBar.visibility=View.VISIBLE

        bindViews()
        Handler(Looper.getMainLooper()).postDelayed({
           progressBar.visibility=View.GONE
        }, 500)

        super.onResume()
    }



    private fun bindViews() {

        total=order!!.cart.totalPrice
        textViewTotal=view1.findViewById(R.id.textViewTotalCost)
        textViewTotal.text= total.toString()+" EGP "
        backImageView=view1.findViewById(R.id.back)



        val productId:ArrayList<String> = ArrayList<String>()
        val productHash:HashSet<String> = HashSet<String>()
        val products:ArrayList<Product> = ArrayList<Product>()
        val cart=order!!.cart
        for(ids in cart.itemsIdPriceList) {
            productId.add(ids.key)
            productHash.add(ids.key)

        }
        val dbreference=FirebaseDatabase.getInstance().reference
        dbreference.child("Product").orderByChild("store_id").equalTo(order!!.cart.store_id).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (dsp in snapshot.children) {
                            val name = dsp.child("name").value.toString()
                            val id = dsp.child("id").value.toString()
                            val copies = dsp.child("copies").value.toString()
                            val available = when (copies) {
                                "0" -> "Out Of Stock"
                                else -> "Available"
                            }
                            val price = dsp.child("price").value.toString()
                            val description = dsp.child("description").value.toString()
                            val imagePathProduct = dsp.child("imagePathProduct").value.toString()
                            val subCategory = dsp.child("subcategory").value.toString()
                            val imagePathsuri = dsp.child("uriPaths").value as ArrayList<String>


                            val p: Product = Product(
                                name,
                                id,
                                copies.toInt(),
                                available,
                                price.toDouble(),
                                description,
                                imagePathProduct,
                                order!!.cart!!.store_id,
                                subCategory,
                                imagePathsuri
                            )
                            if (productHash.contains(id)) {
                                products.add(p)
                            }

                        }
                    }
                    recyclerViewItems=view1.findViewById(R.id.recyclerViewOrder)
                    val linearLayoutManager=LinearLayoutManager(requireContext())
                    val adapter= OrderAdapter(cart,products)
                    linearLayoutManager.orientation=LinearLayoutManager.VERTICAL
                    recyclerViewItems.layoutManager=linearLayoutManager
                    recyclerViewItems.adapter=adapter


                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
        )}


}
