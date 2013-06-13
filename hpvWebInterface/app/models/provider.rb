class Provider < ActiveRecord::Base
  # attr_accessible :title, :body
  attr_accessible :id
  
  validates_presence_of :id
end
