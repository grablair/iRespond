class CreateProviders < ActiveRecord::Migration
  def change
    create_table :providers, :id => false do |t|
      t.uuid :id, :primary_key => true
      t.timestamps
    end
  end
end
