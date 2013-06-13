class ProviderController < ApplicationController
  def index
    providers = Provider.find(:all)
    render :json => providers
  end

  def create
    provider = Provider.new
    provider.id = params[:id]
    
    if provider.save
      render :json => provider, :status => :created
    else
      render :json => provider.errors, :status => :unprocessable_entity
    end
  end

  def destroy
    provider = Provider.find(params[:id])
    
    provider.destroy
    
    render :json => { :destroyed => true }
  end
end
