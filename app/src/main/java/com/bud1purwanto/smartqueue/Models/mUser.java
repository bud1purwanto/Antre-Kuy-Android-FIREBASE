package com.bud1purwanto.smartqueue.Models;

/**
 * Created by root on 5/9/18.
 */

public class mUser {
    public String nik;
    public String nama;
    public String tempat_lahir;
    public String tanggal_lahir;
    public String alamat;
    public String agama;
    public String status;
    public String pekerjaan;
    public String email;
    public String foto;
    public String antre;
    public String uid;

    public mUser() {
    }

    public mUser(String nik, String nama, String tempat_lahir, String tanggal_lahir, String alamat, String agama, String status, String pekerjaan, String email, String foto, String antre, String uid) {
        this.nik = nik;
        this.nama = nama;
        this.tempat_lahir = tempat_lahir;
        this.tanggal_lahir = tanggal_lahir;
        this.alamat = alamat;
        this.agama = agama;
        this.status = status;
        this.pekerjaan = pekerjaan;
        this.email = email;
        this.foto = foto;
        this.antre = antre;
        this.uid = uid;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getTempat_lahir() {
        return tempat_lahir;
    }

    public void setTempat_lahir(String tempat_lahir) {
        this.tempat_lahir = tempat_lahir;
    }

    public String getTanggal_lahir() {
        return tanggal_lahir;
    }

    public void setTanggal_lahir(String tanggal_lahir) {
        this.tanggal_lahir = tanggal_lahir;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getAgama() {
        return agama;
    }

    public void setAgama(String agama) {
        this.agama = agama;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPekerjaan() {
        return pekerjaan;
    }

    public void setPekerjaan(String pekerjaan) {
        this.pekerjaan = pekerjaan;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getAntre() {
        return antre;
    }

    public void setAntre(String antre) {
        this.antre = antre;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
