class Patient < ActiveRecord::Base
  attr_accessible :id, :address, :area, :birth_day, :family_name, :fathers_name, :given_name, :local_id, :mothers_name, :notes, :phone_number, :sms_reminders, :first_dose_date, :second_dose_date, :third_dose_date, :photo_url

  before_save :default_values
  
  def default_values
    if self.local_id.nil?
      o =  [('0'..'9')].map{|i| i.to_a}.flatten
      o2 =  [('1'..'9')].map{|i| i.to_a}.flatten
      
      begin
        id = o2[rand(o2.length)] + (1..10).map{ o[rand(o.length)] }.join
      end while !Patient.where(:local_id => id).empty?
      
      self.local_id = id
    end
    
    self.notes ||= ""
  end
  
  validates_presence_of :id
  validates_presence_of :family_name
  validates_presence_of :given_name
  
end
