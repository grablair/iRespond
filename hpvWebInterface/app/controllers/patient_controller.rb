class PatientController < ApplicationController
  def show
    patient = Patient.find(params[:id])
    render :json => patient
  end

  def create
    patient = Patient.new
    
    params.delete("controller")
    params.delete("action")

    params.each do |k,v|
      patient.send(k + "=", v)
    end
    
    if patient.save
      render :json => patient, :status => :created
    else
      render :json => patient.errors, :status => :unprocessable_entity
    end
  end

  def update
    patient = Patient.find(params[:id])
    
    params.delete("controller")
    params.delete("action")

    params.each do |k,v|
      patient.send(k + "=", v)
    end
    
    if patient.save
      render :json => patient
    else
      render :json => patient.errors, :status => :unprocessable_entity
    end
  end

  def destroy
    patient = Patient.find(params[:id])
    
    patient.destroy
    
    render :json => { :destroyed => true }
  end
end
