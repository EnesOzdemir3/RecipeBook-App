package com.enesozdemir.tarifkitabiuygulamasi.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.enesozdemir.tarifkitabiuygulamasi.databinding.FragmentTarifBinding
import com.enesozdemir.tarifkitabiuygulamasi.model.Tarif
import com.enesozdemir.tarifkitabiuygulamasi.roomdb.TarifDAO
import com.enesozdemir.tarifkitabiuygulamasi.roomdb.TarifDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException


class TarifFragment : Fragment() {
    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher :ActivityResultLauncher<String>
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private var secilenGorsel : Uri? = null
    private var secilenBitmap : Bitmap? = null
    private val mDisposable = CompositeDisposable()
    private lateinit var db : TarifDatabase
    private lateinit var tarifDao: TarifDAO
    private var secilenTarif : Tarif? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"Tarifler").allowMainThreadQueries().build()
        tarifDao=db.tarifDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener{gorselSec(it)}
        binding.buttonKaydet.setOnClickListener{kaydet(it)}
        binding.buttonSil.setOnClickListener{sil(it)}

        arguments?.let {
           val bilgi = TarifFragmentArgs.fromBundle(it).bilgi

            if (bilgi=="yeni"){
                binding.buttonSil.isEnabled = false
                binding.buttonKaydet.isEnabled = true
            }else{
                binding.buttonSil.isEnabled = true
                binding.buttonKaydet.isEnabled = false
                val id = TarifFragmentArgs.fromBundle(it).id
                mDisposable.add(
                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )
            }
        }



        }
        private fun handleResponse(tarif : Tarif){
            binding.yemekIsMText.setText(tarif.isim)
            binding.malzemeText.setText(tarif.malzeme)
            val bitmap = BitmapFactory.decodeByteArray(tarif.gorsel,0,tarif.gorsel.size)
            binding.imageView.setImageBitmap(bitmap)
            secilenTarif = tarif
    }

    fun kaydet(view: View){
        val isim = binding.yemekIsMText.text.toString()
        val malzeme = binding.malzemeText.text.toString()

        if(secilenBitmap!=null){
            val kucukBitmap=kucukBitmapOlustur(secilenBitmap!!,300)
            val outPutStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outPutStream)
            val byteDizisi = outPutStream.toByteArray()

            val tarif = Tarif(isim,malzeme,byteDizisi)

            mDisposable.add(
                tarifDao.insert(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForInsert))

        }
    }
    private fun handleResponseForInsert(){
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    fun sil(view: View){
        if (secilenTarif != null) {
            mDisposable.add(
                tarifDao.delete(tarif = secilenTarif!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsert)

            )
        }


    }

    fun gorselSec(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş oluyor izin istemem gerekiyor
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    //snackbar ile neden izin istediğimizi göstermemiz gerekiyor
                    Snackbar.make(view,"galeriden görsel seçmen için izin vermen lazım",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver",View.OnClickListener
                    {
                        //tekrardan izin istenecek
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                } else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{
                //izin zaten verildi
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

        } else {

            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //izin verilmemiş oluyor izin istemem gerekiyor
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //snackbar ile neden izin istediğimizi göstermemiz gerekiyor
                    Snackbar.make(
                        view,
                        "galeriden görsel seçmen için izin vermen lazım",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("İzin Ver", View.OnClickListener
                    {
                        //tekrardan izin istenecek
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                } else {
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                //izin zaten verildi
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher(){
        activityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if (result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                    if (intentFromResult != null){
                        secilenGorsel = intentFromResult.data
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    requireActivity().contentResolver,
                                    secilenGorsel!!
                                )
                                secilenBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(secilenBitmap)
                            } else {

                                secilenBitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    secilenGorsel
                                )
                                binding.imageView.setImageBitmap(secilenBitmap)
                            }
                        } catch (e: IOException){
                            println(e.localizedMessage)
                        }

                    }
            }

        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result->
            if (result){
                //izin verildi
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(requireContext(),"izin verilmedi",Toast.LENGTH_LONG).show()
            }

        }

    }

    private fun kucukBitmapOlustur(kullanicininSectigiBitmap : Bitmap,maximumBoyut : Int): Bitmap
    {
        var width = kullanicininSectigiBitmap.width
        var height= kullanicininSectigiBitmap.height

        val bitmapOrani : Double = width.toDouble()/height.toDouble()
        if(bitmapOrani > 1){
            width = maximumBoyut
            val kisaltilmisYukseklik = width/bitmapOrani
            height=kisaltilmisYukseklik.toInt()
        }else{
            height = maximumBoyut
            val kisaltilmisGenislik = height*bitmapOrani
            width=kisaltilmisGenislik.toInt()
        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mDisposable.clear()
    }

    companion object {

    }
}