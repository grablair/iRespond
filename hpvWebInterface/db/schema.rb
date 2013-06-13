# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended to check this file into your version control system.

ActiveRecord::Schema.define(:version => 20130527100308) do

  create_table "patients", :force => true do |t|
    t.string   "local_id"
    t.string   "family_name"
    t.string   "given_name"
    t.string   "mothers_name"
    t.string   "fathers_name"
    t.string   "phone_number"
    t.boolean  "sms_reminders"
    t.string   "address"
    t.string   "area"
    t.date     "birth_day"
    t.string   "notes"
    t.date     "first_dose_date"
    t.date     "second_dose_date"
    t.date     "third_dose_date"
    t.datetime "created_at",       :null => false
    t.datetime "updated_at",       :null => false
  end

  add_index "patients", ["id"], :name => "sqlite_autoindex_patients_1", :unique => true

  create_table "providers", :force => true do |t|
    t.datetime "created_at", :null => false
    t.datetime "updated_at", :null => false
    t.string   "photo_url"
  end

  add_index "providers", ["id"], :name => "sqlite_autoindex_providers_1", :unique => true

end
